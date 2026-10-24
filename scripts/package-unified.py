#!/usr/bin/env python3
"""Package the already-built, already-tested Unified JAR for a first playable alpha.

No downloading, modifying or rebuilding bytecode here: a checksum, manifest,
edition marker, JUnit XMLs and real Forge smoke PASS are required to package.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import sys
import time
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
BUILT = ROOT / "build/unified"
JAR = BUILT / "libs/AlienClient-Unified-1.8.9.jar"
SMOKE = BUILT / "combat-smoke/result.txt"
TESTS = BUILT / "test-results"
DISTRIBUTION = BUILT / "distribution"
CONTEXT = BUILT / "validation-inputs.json"
FILENAME = "AlienClient-Unified-1.8.9.jar"
PACKAGE = "AlienClient-Unified-Primeiro-Teste.zip"
MAX_JAR_SIZE = 32 * 1024 * 1024


def require(condition, explanation):
    if not condition:
        raise ValueError(explanation)


def hash_files(paths, base):
    """Stable content identity, independent of checkout mtimes or absolute paths."""
    digest = hashlib.sha256()
    for path in sorted(paths):
        digest.update(str(path.relative_to(base)).replace(os.sep, "/").encode("utf-8"))
        digest.update(b"\0")
        digest.update(hashlib.sha256(path.read_bytes()).digest())
    return digest.hexdigest()


def validation_inputs():
    require(JAR.is_file(), "Build the Unified JAR before beginning validation")
    junit = list(TESTS.glob("TEST-*.xml"))
    require(len(junit) >= 10, "JUnit suite is incomplete")
    classes = list((BUILT / "classes/main").rglob("*.class"))
    resources = [p for p in (BUILT / "resources/main").rglob("*") if p.is_file()]
    require(bool(classes) and bool(resources), "Compiled Unified runtime inputs are missing")
    source_paths = [ROOT / "build.gradle", ROOT / "scripts/verify-edition-artifact.py"]
    for directory in ("src/main", "src/unified", "src/test", "src/unifiedTest", "src/combatSmoke", "src/ultimateSmoke", "src/sceneSmoke", "src/studioSmoke"):
        source_paths.extend(p for p in (ROOT / directory).rglob("*") if p.is_file())
    require(all(p.is_file() for p in source_paths), "Build provenance inputs are missing")
    return {
        "jar_sha256": hashlib.sha256(JAR.read_bytes()).hexdigest(),
        "junit_sha256": hash_files(junit, BUILT),
        "runtime_sha256": hash_files(classes + resources, BUILT),
        "source_sha256": hash_files(source_paths, ROOT),
    }


def begin_validation():
    receipt = validation_inputs()
    # A previous successful game run cannot satisfy this new validation window.
    if SMOKE.exists():
        SMOKE.unlink()
    receipt["started_ns"] = time.time_ns()
    CONTEXT.parent.mkdir(parents=True, exist_ok=True)
    CONTEXT.write_text(json.dumps(receipt, sort_keys=True, indent=2) + "\n", encoding="utf-8")
    print("Validation window opened for Unified JAR " + receipt["jar_sha256"])


def verify_validation_window():
    require(CONTEXT.is_file(), "Run --begin-validation after build and before the graphical test")
    receipt = json.loads(CONTEXT.read_text(encoding="utf-8"))
    for key, value in validation_inputs().items():
        require(receipt.get(key) == value,
                "Validation inputs changed after --begin-validation: " + key)
    require(SMOKE.is_file(), "Real Minecraft/Forge smoke result is missing")
    started = receipt.get("started_ns")
    require(isinstance(started, int) and SMOKE.stat().st_mtime_ns >= started,
            "Graphical PASS predates the current validation window")
    return receipt


def produce():
    require(JAR.is_file() and 0 < JAR.stat().st_size <= MAX_JAR_SIZE,
            "A verified Unified release JAR is required")
    jar_bytes = JAR.read_bytes()
    digest = hashlib.sha256(jar_bytes).hexdigest()

    with zipfile.ZipFile(JAR) as jar:
        names = set(jar.namelist())
        require("dev/alien/client/AlienClient.class" in names,
                "Missing real AlienClient mod entry point")
        require("META-INF/alienclient/edition.json" in names, "Missing edition marker")
        manifest = jar.read("META-INF/MANIFEST.MF").decode("utf-8")
        edition = json.loads(jar.read("META-INF/alienclient/edition.json"))
        require("Alien-Edition: unified" in manifest and "Minecraft-Version: 1.8.9" in manifest,
                "The JAR is not the Forge 1.8.9 Unified build")
        require(edition.get("id") == "unified" and edition.get("configDirectory") == "alien-client",
                "Compiled edition marker was replaced")
        require(not any("smoke/" in entry.lower() or "combatsmoke" in entry.lower()
                        for entry in names if entry.endswith(".class")),
                "Test-only classes must not ship to player installations")

    receipt = verify_validation_window()
    smoke_lines = SMOKE.read_text(encoding="utf-8").splitlines()
    require(bool(smoke_lines) and re.fullmatch(r"PASS [1-9][0-9]* checks", smoke_lines[0]),
            "Minecraft/Forge graphical test must finish with PASS")
    require(any("Combat Studio" in line for line in smoke_lines[1:]),
            "Smoke report lacks the real Combat Studio verification")
    junit = list(TESTS.glob("TEST-*.xml"))
    require(len(junit) >= 10, "JUnit suite is incomplete")
    total = 0
    for report in junit:
        suite = ET.parse(str(report)).getroot()
        require(int(suite.get("errors", 0)) == 0 and int(suite.get("failures", 0)) == 0,
                "A JUnit suite has errors or failures: " + report.name)
        total += int(suite.get("tests", 0))
    require(total >= 100, "Expected full regression suite, not a partial test selection")

    DISTRIBUTION.mkdir(parents=True, exist_ok=True)
    instructions = """ALIEN CLIENT UNIFIED - PRIMEIRO TESTE (EXPERIMENTAL)
================================================
Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8.

INSTALAR NO WINDOWS
1) Instale um perfil Minecraft Java 1.8.9 com Forge 11.15.1.2318 e Java 8.
2) Feche o Minecraft. No Explorador, abra %APPDATA%\\.minecraft\\mods.
   Outros launchers podem usar uma pasta do jogo diferente.
3) Faça backup e REMOVA da pasta mods qualquer JAR antigo Alien Client
   (Legit, Full ou versoes anteriores). So pode haver um modid alienclient.
4) Copie SOMENTE AlienClient-Unified-1.8.9.jar para a pasta mods.
5) Inicie o Minecraft Forge 1.8.9, abra um mundo local e aperte Right Shift.
6) Explore Combat Studio, World Studio, HUD Editor e o ClickGUI.

COMPATIBILIDADE E RISCOS
- Build experimental, NAO e uma release estavel nem um launcher independente.
- Compilado/testado em Linux Java 8/Forge 1.8.9 com display virtual/Mesa.
- Ainda NAO testado nesta etapa com Windows, GPU real, OptiFine ou servidores.
- Combat comeca desativado. Presets Legit/Full podem violar regras PvP.
- Fake anti-aim, silent aim e bypass nao estao implementados.
- Config antiga Legit e reutilizada em config/alien-client/; faca backup.
- Config antiga Full em config/alien-client-full/ NAO migra automaticamente.

CODIGO E RELATORIOS
https://github.com/AttackAlien/Alien-Client
"""
    run_id = os.environ.get("GITHUB_RUN_ID", "execucao-local")
    revision = os.environ.get("GITHUB_SHA", "codigo-local")
    validation = (
        "ALIEN CLIENT UNIFIED - VALIDACAO DA COMPILACAO\n"
        "GitHub Actions run: " + run_id + "\n"
        "Git commit: " + revision + "\n"
        "JUnit: " + str(total) + " testes, nenhum erro ou falha\n"
        "Minecraft 1.8.9 (Linux/display virtual/Mesa): " + smoke_lines[0] + "\n"
        "JAR, classes, recursos, fontes e JUnit verificados pela mesma janela de validacao.\n"
        "SHA256 dos fontes/build: " + receipt["source_sha256"] + "\n"
        "SHA256 das classes/recursos: " + receipt["runtime_sha256"] + "\n"
        "SHA256 dos relatorios JUnit: " + receipt["junit_sha256"] + "\n"
        "Forge real: Combat Studio, perfis, JSON, dummy local e framebuffer\n"
        "Nao representa teste Windows/OptiFine/GPU real/servidor externo.\n"
        "SHA256: " + digest + "  " + FILENAME + "\n"
    )
    members = {
        FILENAME: jar_bytes,
        "LEIA-ME-INSTALACAO.txt": instructions.encode("utf-8"),
        "SHA256SUMS.txt": (digest + "  " + FILENAME + "\n").encode("utf-8"),
        "RESULTADO-DOS-TESTES.txt": validation.encode("utf-8"),
    }
    with zipfile.ZipFile(DISTRIBUTION / PACKAGE, "w", zipfile.ZIP_DEFLATED) as archive:
        for filename, data in members.items():
            archive.writestr(filename, data)

    for filename, data in members.items():
        (DISTRIBUTION / filename).write_bytes(data)
    print("PASS: " + PACKAGE + "; JUnit: " + str(total) + "; " +
          smoke_lines[0] + "; SHA256: " + digest)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--begin-validation", action="store_true",
                        help="Freeze built/tested inputs and invalidate an older graphical PASS")
    arguments = parser.parse_args()
    try:
        if arguments.begin_validation:
            begin_validation()
        else:
            produce()
    except (ValueError, OSError, KeyError, TypeError, zipfile.BadZipFile, ET.ParseError) as exc:
        print("Unified installer packaging failed: " + str(exc), file=sys.stderr)
        sys.exit(1)
