#!/usr/bin/env python3
"""Publish existing verified binaries only after the integrated main passes its CIs."""
from pathlib import Path
import hashlib
import json
import os
import re
import shutil
import subprocess
import time
import xml.etree.ElementTree as ET
import zipfile

REPO = 'AttackAlien/Alien-Client'
TAG = 'v0.10.0-alpha.1'
ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'release-output'
PROOFS = OUT / 'proofs'


def run(*args):
    return subprocess.check_output(list(args), cwd=ROOT, text=True).strip()


def api(path):
    return json.loads(run('gh', 'api', 'repos/' + REPO + '/' + path))


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def write(path, value):
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n')


def sha(path):
    digest = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(512 * 1024), b''):
            digest.update(block)
    return digest.hexdigest()


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def ci(run_id, expected_sha, workflow):
    result = api('actions/runs/' + run_id)
    require(result['repository']['full_name'] == REPO and result['head_sha'] == expected_sha
            and result['path'] == workflow, 'CI source or workflow mismatch: ' + run_id)
    return result


def download(run_id, name, target):
    target.mkdir(parents=True)
    run('gh', 'run', 'download', run_id, '--repo', REPO, '-n', name, '-D', str(target))


def junit(folder):
    suites = [ET.parse(p).getroot() for p in folder.glob('TEST-*.xml')]
    counts = {key: sum(int(s.get(key, 0)) for s in suites)
              for key in ('tests', 'failures', 'errors', 'skipped')}
    require(counts == dict(tests=223, failures=0, errors=0, skipped=0), 'JUnit: ' + str(counts))
    return counts


def trx(path):
    ns = {'t': 'http://microsoft.com/schemas/VisualStudio/TeamTest/2010'}
    counts = ET.parse(path).getroot().find('t:ResultSummary/t:Counters', ns).attrib
    require(counts['total'] == counts['passed'] == '31' and counts['failed'] == '0'
            and counts['notExecuted'] == '0', 'Launcher tests: ' + str(counts))
    return {key: int(counts[key]) for key in ('total', 'passed', 'failed', 'notExecuted')}


def main():
    require(os.environ.get('GITHUB_REPOSITORY') == REPO, 'Unexpected repository')
    require(os.environ.get('GITHUB_REF') == 'refs/heads/main', 'Publish only from main')
    source = os.environ['ALIEN_VALIDATED_MAIN']
    unified_id = os.environ['ALIEN_MAIN_UNIFIED_RUN']
    launcher_id = os.environ['ALIEN_MAIN_LAUNCHER_RUN']
    require(re.fullmatch('[0-9a-f]{40}', source) is not None
            and unified_id.isdigit() and launcher_id.isdigit(), 'Invalid validation identifiers')
    OUT.mkdir(exist_ok=True)
    PROOFS.mkdir(exist_ok=True)
    integration = read(ROOT / 'docs/releases/v0.10.0-alpha.1/integration.json')
    client = read(ROOT / 'docs/polish-results/2026-11-04/client-summary.json')
    launcher = read(ROOT / 'docs/polish-results/2026-11-04/launcher-summary.json')
    pin = read(ROOT / 'launcher/client-source.json')
    original_client = ci(client['runId'], client['sourceCommit'], '.github/workflows/unified-client-ci.yml')
    original_launcher = ci(launcher['launcherRun'], launcher['launcherSource'], '.github/workflows/alien-launcher.yml')
    require(original_client['conclusion'] == original_launcher['conclusion'] == 'success', 'Original builds not successful')
    run('git', 'fetch', '--no-tags', '--depth=1', 'origin', source, client['sourceCommit'], launcher['launcherSource'])
    identity = {}
    for path, expected in integration['runtimePaths'].items():
        current = run('git', 'rev-parse', 'HEAD:' + path)
        main_object = run('git', 'rev-parse', source + ':' + path)
        original = run('git', 'rev-parse', expected['validatedRevision'] + ':' + path)
        require(current == main_object == original == expected['gitObject'], 'Runtime changed: ' + path)
        identity[path] = current
    merged = []
    for entry in integration['openPullRequests']:
        pr = api('pulls/' + str(entry['number']))
        require(pr['merged'] and pr['base']['ref'] == 'main' and pr['head']['sha'] == entry['head'],
                'PR not integrated: ' + str(entry['number']))
        merged.append(dict(number=pr['number'], head=pr['head']['sha'], mergedAt=pr['merged_at'], mergeCommit=pr['merge_commit_sha']))
    deadline = time.monotonic() + 30 * 60
    while True:
        unified = ci(unified_id, source, '.github/workflows/unified-client-ci.yml')
        main_launcher = ci(launcher_id, source, '.github/workflows/alien-launcher.yml')
        for item in (unified, main_launcher):
            require(item['status'] != 'completed' or item['conclusion'] == 'success',
                    'Main CI failed: ' + item['html_url'])
        if all(item['status'] == 'completed' for item in (unified, main_launcher)):
            break
        require(time.monotonic() < deadline, 'Main CI validation deadline')
        print('Waiting for main:', unified['status'], main_launcher['status'], flush=True)
        time.sleep(20)
    main_client = PROOFS / 'main-client'
    main_core = PROOFS / 'main-launcher-core'
    main_win = PROOFS / 'main-launcher-windows'
    download(unified_id, 'alien-unified-' + unified_id, main_client)
    download(launcher_id, 'alien-launcher-core-' + launcher_id, main_core)
    download(launcher_id, 'alien-launcher-win64-' + launcher_id, main_win)
    counts = junit(main_client / 'test-results')
    expected = {'smoke': 161, 'sky-smoke': 37, 'combat-smoke': 24,
                'ultimate-smoke': 328, 'scene-smoke': 71, 'studio-smoke': 142}
    for phase, number in expected.items():
        require((main_client / phase / 'result.txt').read_text().splitlines()[0] == 'PASS %d checks' % number,
                'Graphical validation failed: ' + phase)
    linux = trx(main_core / 'linux.trx')
    windows = trx(main_win / 'launcher-test-results/windows.trx')
    ui = main_win / 'launcher-test-results/ui'
    require((ui / 'result.txt').read_text(encoding='utf-8-sig').splitlines()[0] == 'PASS 46 checks', 'Native UI failed')
    require('INSTALL_PASS' in (main_win / 'launcher-test-results/windows-install.txt').read_text(encoding='utf-8-sig'), 'Forge installation failed')
    with zipfile.ZipFile(main_client / 'libs/AlienClient-Unified-1.8.9.jar') as fresh, zipfile.ZipFile(ROOT / 'launcher/payload/AlienClient-Unified-1.8.9.jar') as shipping:
        names = {i.filename for i in shipping.infolist() if not i.is_dir()}
        require(names == {i.filename for i in fresh.infolist() if not i.is_dir()}, 'Main JAR entry set differs')
        require(all(fresh.read(name) == shipping.read(name) for name in names), 'Main JAR entry bytes differ from shipping JAR')
    artifacts = {
        'AlienLauncher-Windows-x64-0.3.0-alpha.7.zip': ROOT / 'downloads/launcher/v0.3.0-alpha.7/AlienLauncher-Windows-x64-0.3.0-alpha.7.zip',
        'AlienClient-Unified-1.8.9.jar': ROOT / 'downloads/v0.10.0-alpha.1/AlienClient-Unified-1.8.9.jar',
        'AlienClient-Unified-Primeiro-Teste.zip': ROOT / 'downloads/v0.10.0-alpha.1/AlienClient-Unified-Primeiro-Teste.zip',
        'Alien-Client-0.10-customizacoes-full.mp4': ROOT / 'captures/video-atual/Alien-Client-0.10-customizacoes-full.mp4',
        'LEIA-ME-RELEASE.md': ROOT / '.github/release/LEIA-ME-RELEASE.md'}
    for name, path in artifacts.items():
        shutil.copyfile(path, OUT / name)
    require(sha(OUT / 'AlienClient-Unified-1.8.9.jar') == pin['sha256'] == client['jarSha256'], 'Client hash mismatch')
    require(sha(OUT / 'AlienLauncher-Windows-x64-0.3.0-alpha.7.zip') == launcher['zipSha256'], 'Launcher hash mismatch')
    movie = read(ROOT / 'docs/polish-results/2026-11-04/video/video-validation.json')
    require(sha(OUT / 'Alien-Client-0.10-customizacoes-full.mp4') == movie['sha256'], 'Movie hash mismatch')
    capture_manifest = read(ROOT / 'captures/atual/unified/validation.json')
    require(len(capture_manifest['screenshots']) == 297, 'Unexpected current capture count')
    with zipfile.ZipFile(OUT / 'AlienClient-0.10-Capturas-Reais.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
        for folder in ('captures/atual/unified', 'captures/launcher-atual'):
            for path in sorted((ROOT / folder).rglob('*')):
                if path.is_file():
                    archive.write(path, str(path.relative_to(ROOT)))
        archive.write(ROOT / 'captures/README.md', 'captures/README.md')
    for name, data in [('main-unified-run.json', unified), ('main-launcher-run.json', main_launcher), ('merged-prs.json', merged)]:
        write(PROOFS / name, data)
    with zipfile.ZipFile(OUT / 'AlienClient-Validation-Reports.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(PROOFS.rglob('*')):
            if path.is_file() and path.suffix in ('.xml', '.trx', '.json', '.txt'):
                archive.write(path, str(path.relative_to(PROOFS)))
    for path in OUT.glob('*'):
        if path.suffix in ('.zip', '.jar'):
            with zipfile.ZipFile(path) as archive:
                require(archive.testzip() is None, 'Invalid archive: ' + path.name)
    with zipfile.ZipFile(OUT / 'AlienLauncher-Windows-x64-0.3.0-alpha.7.zip') as archive:
        require(hashlib.sha256(archive.read('payload/' + pin['jar'])).hexdigest() == pin['sha256'], 'Bundled client mismatch')
    assets = {p.name: dict(bytes=p.stat().st_size, sha256=sha(p)) for p in OUT.iterdir() if p.is_file()}
    write(OUT / 'VALIDATION.json', dict(tag=TAG, targetCommit=source, publisherCommit=run('git', 'rev-parse', 'HEAD'),
          runtimeObjects=identity, originalClientBuild=client, originalLauncherBuild=launcher,
          mainValidation=dict(unifiedRun=unified_id, launcherRun=launcher_id, junit=counts, graphicalChecks=sum(expected.values()),
                              launcherLinux=linux, launcherWindows=windows, nativeUiChecks=46,
                              forgeInstallation='PASS', shippingJarEntriesEqualMainCI=True),
          mergedPullRequests=merged, video=movie, assets=assets,
          limitations=['No licensed Microsoft login validation', 'No external multiplayer or physical GPU/Windows gameplay validation', 'Emotes and auras are local']))
    files = sorted(p for p in OUT.iterdir() if p.is_file())
    (OUT / 'SHA256SUMS.txt').write_text(''.join(sha(p) + '  ' + p.name + '\n' for p in files))
    base = 'https://github.com/' + REPO
    notes = f'''Minecraft Java **1.8.9 / Forge 11.15.1.2318 / Java 8**, com **launcher Windows x64 0.3.0-alpha.7**.

Os 16 PRs pendentes foram integrados à main pelo PR #26. Inclui HUD Ultimate, Full opcional, redesign 0.4, perfis, cosméticos, capas e chapéus, trilhas desenhadas, emotes articulados, auras reativas e céu Realistic com nuvens suaves.

- **Windows:** baixe e extraia o ZIP do launcher inteiro; abra AlienLauncher.exe mantendo payload ao lado.
- **Forge já instalado:** use o JAR ou o ZIP Primeiro-Teste. Mantenha um único JAR Alien em mods.
- **Mídia real:** vídeo contínuo de 105,77s a 30 FPS e ZIP com 297 PNGs do jogo, seis do launcher e GIFs atuais. Mediana renderizada de 57,6 FPS nesta tomada; sem imagens simuladas ou interpoladas.

**Main validada:** 223 JUnit, 763 checks gráficos no Forge, 31 testes Linux + 31 Windows do launcher, 46 checks da janela nativa e instalação Forge/Java 8 aprovados. [CI Unified]({unified['html_url']}) · [CI launcher]({main_launcher['html_url']}). Os 12 testes de empacotamento fazem parte do workflow aprovado.

Tag na main integrada `{source}`. Os pacotes originais dos CIs anteriores foram preservados; seus fontes de execução coincidem com a main e todos os arquivos dentro do JAR recompilado na main são idênticos aos do JAR distribuído. VALIDATION.json registra as origens, resultados e hashes; AlienClient-Validation-Reports.zip contém recibos/XMLs. Confira SHA256SUMS.txt após baixar.

[Guia e auditoria da integração]({base}/blob/{source}/docs/releases/v0.10.0-alpha.1/README.md) · [Fotos atuais]({base}/blob/{source}/captures/README.md).

**Release alpha.** Login/licença Microsoft, GPU física, multiplayer externo, OptiFine e gameplay em Windows físico não validados. Interface e instalação do launcher foram testadas no Windows Server 2022; o jogo, em Linux com OpenGL por software. Emotes e auras são locais.
'''
    (OUT / 'release-notes.md').write_text(notes)
    releases = api('releases?per_page=100')
    matches = [item for item in releases if item['tag_name'] == TAG]
    require(len(matches) == 1 and matches[0]['draft'], 'Expected a single unpublished draft')
    draft = matches[0]
    require(draft['target_commitish'] == source, 'Unexpected release target')
    upload = sorted(p for p in OUT.iterdir() if p.is_file() and p.name != 'release-notes.md')
    run('gh', 'release', 'upload', TAG, *map(str, upload), '--repo', REPO, '--clobber')
    uploaded = api('releases/' + str(draft['id']))
    require({a['name'] for a in uploaded['assets']} == {p.name for p in upload}, 'Release asset set differs')
    for item in uploaded['assets']:
        path = OUT / item['name']
        require(item['state'] == 'uploaded' and item['size'] == path.stat().st_size, 'Incomplete asset: ' + path.name)
        if item.get('digest'):
            require(item['digest'] == 'sha256:' + sha(path), 'Server digest mismatch: ' + path.name)
    publication = PROOFS / 'publication.json'
    write(publication, dict(draft=False, prerelease=True, body=notes, target_commitish=source))
    published = json.loads(run('gh', 'api', 'repos/' + REPO + '/releases/' + str(draft['id']), '--method', 'PATCH', '--input', str(publication)))
    require(not published['draft'] and published['prerelease'], 'Release did not publish')
    require(api('git/ref/tags/' + TAG)['object']['sha'] == source, 'Tag target mismatch')
    print('Published ' + published['html_url'], flush=True)


if __name__ == '__main__':
    main()
