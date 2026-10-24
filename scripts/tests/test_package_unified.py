"""Negative/positive tests for the real Unified installer packager.

Runs on a disposable copy of the JAR, JUnit XMLs and Minecraft smoke PASS.
No player files and no real build artifacts are modified.
"""
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import shutil
import tempfile
import unittest
import zipfile
import xml.etree.ElementTree as ET

SOURCE = Path(__file__).resolve().parents[1] / "package-unified.py"
spec = importlib.util.spec_from_file_location("alien_unified_packager", str(SOURCE))
packager = importlib.util.module_from_spec(spec)
spec.loader.exec_module(packager)

class UnifiedPackagerRegressionTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="alien-packager-")
        self.addCleanup(self.temp.cleanup)
        built = Path(self.temp.name) / "build" / "unified"
        self.jar = built / "libs" / packager.FILENAME
        self.smoke = built / "combat-smoke" / "result.txt"
        self.tests = built / "test-results"
        self.dist = built / "distribution"
        self.context = built / "validation-inputs.json"
        root = built.parent.parent
        self.jar.parent.mkdir(parents=True)
        self.smoke.parent.mkdir(parents=True)
        self.tests.mkdir(parents=True)
        shutil.copyfile(packager.JAR, self.jar)
        shutil.copyfile(packager.SMOKE, self.smoke)
        for report in packager.TESTS.glob("TEST-*.xml"):
            shutil.copyfile(report, self.tests / report.name)
        shutil.copyfile(packager.CONTEXT, self.context)
        for directory in ("classes/main", "resources/main"):
            shutil.copytree(packager.BUILT / directory, built / directory)
        for directory in ("src/main", "src/unified", "src/test", "src/unifiedTest", "src/combatSmoke", "src/ultimateSmoke", "src/sceneSmoke", "src/studioSmoke"):
            if (packager.ROOT / directory).is_dir():
                shutil.copytree(packager.ROOT / directory, root / directory)
        for name in ("build.gradle", "scripts/verify-edition-artifact.py"):
            (root / name).parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(packager.ROOT / name, root / name)
        original = (packager.JAR, packager.SMOKE, packager.TESTS, packager.DISTRIBUTION,
                    packager.ROOT, packager.BUILT, packager.CONTEXT)
        self.addCleanup(self.restore, original)
        packager.JAR, packager.SMOKE, packager.TESTS, packager.DISTRIBUTION = (
            self.jar, self.smoke, self.tests, self.dist
        )
        packager.ROOT, packager.BUILT, packager.CONTEXT = root, built, self.context

    @staticmethod
    def restore(original):
        (packager.JAR, packager.SMOKE, packager.TESTS, packager.DISTRIBUTION,
         packager.ROOT, packager.BUILT, packager.CONTEXT) = original

    def test_produces_single_unmodified_jar_with_verified_hash(self):
        packager.produce()
        with zipfile.ZipFile(self.dist / packager.PACKAGE) as archive:
            self.assertEqual(set(archive.namelist()), {
                packager.FILENAME, "LEIA-ME-INSTALACAO.txt",
                "SHA256SUMS.txt", "RESULTADO-DOS-TESTES.txt"
            })
            jar = archive.read(packager.FILENAME)
            self.assertEqual(jar, self.jar.read_bytes())
            expected = hashlib.sha256(jar).hexdigest()
            self.assertEqual(archive.read("SHA256SUMS.txt").decode("utf-8"),
                             expected + "  " + packager.FILENAME + "\n")
            summary = archive.read("RESULTADO-DOS-TESTES.txt").decode("utf-8")
            self.assertIn("Minecraft 1.8.9", summary)
            self.assertIn("SHA256: " + expected, summary)
        self.assertEqual((self.dist / packager.FILENAME).read_bytes(), self.jar.read_bytes())

    def test_refuses_unavailable_graphical_result(self):
        self.smoke.unlink()
        with self.assertRaisesRegex(ValueError, "smoke result is missing"):
            packager.produce()
        self.assertFalse(self.dist.exists())

    def test_refuses_false_positive_or_missing_combat_result(self):
        self.smoke.write_text("PASS 24 checks\nNo GUI checks!\n", encoding="utf-8")
        with self.assertRaisesRegex(ValueError, "Combat Studio"):
            packager.produce()
        self.smoke.write_text("FAIL at stage 3\n", encoding="utf-8")
        with self.assertRaisesRegex(ValueError, "must finish with PASS"):
            packager.produce()
        self.assertFalse(self.dist.exists())

    def test_refuses_corrupt_jar(self):
        self.jar.write_bytes(b"NOT_A_FORGE_JAR")
        with self.assertRaises((ValueError, zipfile.BadZipFile)):
            packager.produce()
        self.assertFalse(self.dist.exists())

    def test_refuses_failed_junit(self):
        junit = next(self.tests.glob("TEST-*.xml"))
        root = ET.parse(str(junit)).getroot()
        root.set("failures", "1")
        ET.ElementTree(root).write(str(junit), encoding="utf-8", xml_declaration=True)
        with self.assertRaisesRegex(ValueError, "junit_sha256|JUnit suite"):
            packager.produce()
        self.assertFalse(self.dist.exists())

    def test_refuses_partial_junit_suite(self):
        for path in list(self.tests.glob("TEST-*.xml"))[1:]:
            path.unlink()
        with self.assertRaisesRegex(ValueError, "suite is incomplete"):
            packager.produce()
        self.assertFalse(self.dist.exists())

    def test_refuses_changed_jar_after_validation_begins(self):
        with self.jar.open("ab") as jar:
            jar.write(b"changed-after-graphical-test")
        with self.assertRaisesRegex(ValueError, "jar_sha256"):
            packager.produce()
        self.assertFalse(self.dist.exists())

    def test_refuses_source_or_runtime_changes(self):
        source = packager.ROOT / "build.gradle"
        original = source.read_bytes()
        source.write_bytes(original + b"\n// unbuilt source edit\n")
        with self.assertRaisesRegex(ValueError, "source_sha256"):
            packager.produce()
        source.write_bytes(original)
        compiled = next((packager.BUILT / "classes/main").rglob("*.class"))
        compiled.write_bytes(compiled.read_bytes() + b"changed")
        with self.assertRaisesRegex(ValueError, "runtime_sha256"):
            packager.produce()
        self.assertFalse(self.dist.exists())

    def test_refuses_older_graphical_pass(self):
        os.utime(str(self.smoke), ns=(1, 1))
        with self.assertRaisesRegex(ValueError, "predates"):
            packager.produce()
        self.assertFalse(self.dist.exists())

    def test_refuses_missing_validation_window(self):
        self.context.unlink()
        with self.assertRaisesRegex(ValueError, "--begin-validation"):
            packager.produce()

    def test_new_validation_window_invalidates_previous_pass(self):
        self.assertTrue(self.smoke.is_file())
        packager.begin_validation()
        self.assertFalse(self.smoke.exists())
        receipt = json.loads(self.context.read_text(encoding="utf-8"))
        self.assertEqual(receipt["jar_sha256"], hashlib.sha256(self.jar.read_bytes()).hexdigest())
        self.assertGreater(receipt["started_ns"], 0)

if __name__ == "__main__":
    unittest.main()
