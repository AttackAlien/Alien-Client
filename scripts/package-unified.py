#!/usr/bin/env python3
"""Package the already-built, already-tested Unified JAR for a first playable alpha.

No downloading, modifying or rebuilding bytecode here: a checksum, manifest,
edition marker, JUnit XMLs and real Forge smoke PASS are required to package.
"""
import hashlib
import json
import os
from pathlib import Path
import re
import sys
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
BUILT = ROOT / "build/unified"
JAR = BUILT / "libs/AlienClient-Unified-1.8.9.jar"
SMOKE = BUILT / "combat-smoke/result.txt"
TESTS = BUILT / "test-results"
DISTRIBUTION = BUILT / "distribution"
FILENAME = "AlienClient-Unified-1.8.9.jar"
PACKAGE = "AlienClient-Unified-Primeiro-Teste.zip"
MAX_JAR_SIZE = 32 * 1024 * 1024


def require(condition, explanation):
    if not condition:
        raise ValueError(explanation)


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

    require(SMOKE.is_file(), "Real Minecraft/Forge smoke result is missing")
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
- Compile/testado em GitHub Actions Linux Java 8/Forge 1.8.9 Xorg/Mesa.
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
        "Minecraft 1.8.9 (Xorg/Mesa): " + smoke_lines[0] + "\n"
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
    try:
        produce()
    except (ValueError, OSError, KeyError, zipfile.BadZipFile, ET.ParseError) as exc:
        print("Unified installer packaging failed: " + str(exc), file=sys.stderr)
        sys.exit(1)
