#!/usr/bin/env python3
"""Record profile/thumbnail behavior on disposable files; never starts Minecraft."""
import argparse
import json
import os
from pathlib import Path
import struct
import subprocess
import tempfile
import zlib

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java-home", required=True, type=Path)
    parser.add_argument("--output", type=Path, default=ROOT / "build/audit-local")
    parser.add_argument("--gradle-home", type=Path,
                        default=Path(os.environ.get("GRADLE_USER_HOME", ROOT / ".gradle-user")))
    args = parser.parse_args()
    classes = ROOT / "build/unified/classes/main"
    if not classes.is_dir():
        parser.error("Run the Unified Gradle test task first to compile production classes")
    java = args.java_home / "bin/java"
    javac = args.java_home / "bin/javac"
    dependencies = sorted((args.gradle_home / "caches/modules-2/files-2.1").rglob("*.jar"))
    dependencies += sorted((args.gradle_home / "caches/minecraft/net/minecraftforge").rglob("forgeSrc-*.jar"))
    classpath = os.pathsep.join(map(str, [classes] + dependencies))
    args.output.mkdir(parents=True, exist_ok=True)
    observations = []
    with tempfile.TemporaryDirectory(prefix="alien-audit-probes-") as temp:
        work = Path(temp)
        sources = sorted((ROOT / "tools/audit/JavaProbe").glob("*.java"))
        subprocess.run([str(javac), "-cp", classpath, "-d", temp] + list(map(str, sources)), check=True)
        runtime_classpath = temp + os.pathsep + classpath

        def record(name, main_class, heap="256m", extra=()):
            result = subprocess.run([str(java), "-Djava.awt.headless=true", "-Xmx" + heap,
                                     "-cp", runtime_classpath, main_class] + list(map(str, extra)),
                                    capture_output=True, text=True, timeout=45)
            (args.output / name).write_text(result.stdout + result.stderr, encoding="utf-8")
            observations.append({"file": name, "probeExitCode": result.returncode})
            if result.returncode:
                raise RuntimeError("Probe failed to collect evidence: " + name)

        record("java-profile-reproduction.txt", "ConfigAuditProbe")
        # Synthetic 6000x6000 PNG input, not a screenshot or game-test evidence.
        def chunk(kind, body):
            return (struct.pack(">I", len(body)) + kind + body
                    + struct.pack(">I", zlib.crc32(kind + body) & 0xffffffff))

        compressor = zlib.compressobj(9)
        row = b"\0" + b"\0\0\0" * 6000
        pixels = b"".join(compressor.compress(row) for _ in range(6000)) + compressor.flush()
        fixture = work / "oversized-thumbnail.png"
        fixture.write_bytes(b"\x89PNG\r\n\x1a\n"
                            + chunk(b"IHDR", struct.pack(">IIBBBBB", 6000, 6000, 8, 2, 0, 0, 0))
                            + chunk(b"IDAT", pixels) + chunk(b"IEND", b""))
        for heap in ("64m", "256m"):
            record("thumbnail-method-" + heap + ".txt", "ThumbnailAuditProbe", heap, [fixture])
    (args.output / "probe-execution.json").write_text(json.dumps({
        "notice": "Exit 0 means evidence collection completed, not that the product has no defects.",
        "minecraftStarted": False, "probes": observations}, indent=2) + "\n", encoding="utf-8")
    print("Recorded audit evidence in", args.output)


if __name__ == "__main__":
    main()
