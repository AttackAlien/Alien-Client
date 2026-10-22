#!/usr/bin/env python3
"""Inspect the actual Forge JAR, not a task name or a list of ZIP filenames.

Checks selected source provenance using classfile SourceFile attributes, the
compiled edition provider constants, Forge's runtime @Mod annotation, packaged
metadata, and references to Full-only source types. Uses only Python's stdlib.
"""
import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import struct
import sys
import zipfile


class InvalidArtifact(Exception):
    pass


def require(condition, message):
    if not condition:
        raise InvalidArtifact(message)


def strict_json(data):
    def object_pairs(pairs):
        result = {}
        for key, value in pairs:
            require(key not in result, "Duplicate JSON key: %s" % key)
            result[key] = value
        return result

    def invalid_constant(value):
        raise InvalidArtifact("Non-finite JSON constant: %s" % value)

    return json.loads(data, object_pairs_hook=object_pairs, parse_constant=invalid_constant)


class Reader:
    def __init__(self, data):
        self.data = data
        self.offset = 0

    def take(self, count):
        require(count >= 0 and self.offset + count <= len(self.data), "Truncated classfile")
        result = self.data[self.offset:self.offset + count]
        self.offset += count
        return result

    def u1(self):
        return self.take(1)[0]

    def u2(self):
        return struct.unpack(">H", self.take(2))[0]

    def u4(self):
        return struct.unpack(">I", self.take(4))[0]


def classfile(data):
    reader = Reader(data)
    require(reader.u4() == 0xCAFEBABE, "Invalid classfile magic")
    reader.u2()
    require(reader.u2() == 52, "Production classes must use Java 8 bytecode (major 52)")
    pool = [None] * reader.u2()
    index = 1
    while index < len(pool):
        tag = reader.u1()
        if tag == 1:
            # Modified UTF-8 differs for NUL and surrogate pairs. Those values
            # cannot occur in the names/ASCII metadata checked by this tool.
            pool[index] = (tag, reader.take(reader.u2()).decode("utf-8", errors="replace"))
        elif tag == 3:
            pool[index] = (tag, struct.unpack(">i", reader.take(4))[0])
        elif tag == 4:
            pool[index] = (tag, struct.unpack(">f", reader.take(4))[0])
        elif tag in (5, 6):
            pool[index] = (tag, reader.take(8))
            index += 1
        elif tag in (7, 8, 16, 19, 20):
            pool[index] = (tag, reader.u2())
        elif tag in (9, 10, 11, 12, 17, 18):
            pool[index] = (tag, (reader.u2(), reader.u2()))
        elif tag == 15:
            pool[index] = (tag, (reader.u1(), reader.u2()))
        else:
            raise InvalidArtifact("Unknown classfile constant-pool tag: %s" % tag)
        index += 1

    def constant(number):
        require(0 < number < len(pool) and pool[number] is not None, "Invalid constant-pool reference")
        tag, value = pool[number]
        return constant(value) if tag in (7, 8) else value

    def attributes(source):
        result = {}
        for unused in range(source.u2()):
            name = constant(source.u2())
            require(name not in result, "Duplicate classfile attribute: %s" % name)
            result[name] = source.take(source.u4())
        return result

    def annotation(source):
        name = constant(source.u2())
        values = {}
        for unused in range(source.u2()):
            key = constant(source.u2())
            require(key not in values, "Duplicate annotation element")
            values[key] = element(source)
        return name, values

    def element(source):
        tag = chr(source.u1())
        if tag in "BCDFIJSZs":
            value = constant(source.u2())
            return bool(value) if tag == "Z" else value
        if tag == "e":
            return (constant(source.u2()), constant(source.u2()))
        if tag == "c":
            return constant(source.u2())
        if tag == "@":
            return annotation(source)
        if tag == "[":
            return [element(source) for unused in range(source.u2())]
        raise InvalidArtifact("Invalid annotation element tag")

    reader.u2()  # access flags
    name = constant(reader.u2())
    reader.u2()  # superclass
    for unused in range(reader.u2()):
        reader.u2()
    fields = {}
    for unused in range(reader.u2()):
        access = reader.u2()
        field_name = constant(reader.u2())
        descriptor = constant(reader.u2())
        field_attributes = attributes(reader)
        if "ConstantValue" in field_attributes:
            value_reader = Reader(field_attributes["ConstantValue"])
            require(len(value_reader.data) == 2, "Invalid field ConstantValue")
            fields[field_name] = (descriptor, constant(value_reader.u2()), access)
    for unused in range(reader.u2()):
        reader.take(6)  # method access, name, descriptor
        attributes(reader)
    class_attributes = attributes(reader)
    require(reader.offset == len(data), "Trailing classfile data")
    require("SourceFile" in class_attributes and len(class_attributes["SourceFile"]) == 2,
            "Classfile must retain its SourceFile attribute for provenance inspection")
    source_file = constant(Reader(class_attributes["SourceFile"]).u2())
    annotations = {}
    if "RuntimeVisibleAnnotations" in class_attributes:
        annotation_reader = Reader(class_attributes["RuntimeVisibleAnnotations"])
        for unused in range(annotation_reader.u2()):
            annotation_name, values = annotation(annotation_reader)
            require(annotation_name not in annotations, "Duplicate runtime annotation")
            annotations[annotation_name] = values
        require(annotation_reader.offset == len(annotation_reader.data), "Invalid runtime annotation data")
    strings = {value for item in pool if item is not None for tag, value in [item] if tag == 1}
    references = {constant(number) for number, item in enumerate(pool) if item is not None and item[0] == 7}
    for value in strings:
        references.update(re.findall(r"L([^;<>]+)", value))
    return {"name": name, "source": source_file, "fields": fields,
            "annotations": annotations, "strings": strings, "references": references}


def source_inventory(repository, edition):
    java = set()
    resources = set()
    for root_name in ("main", edition):
        root = repository / "src" / root_name
        java_root = root / "java"
        if java_root.is_dir():
            for file in java_root.rglob("*.java"):
                require(file.resolve().is_relative_to(repository), "Source symlink escapes the repository")
                source = file.read_text(encoding="utf-8")
                package = re.search(r"^\s*package\s+([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\s*;", source, re.MULTILINE)
                require(package is not None, "Java source must declare a package: %s" % file)
                java.add(package.group(1).replace(".", "/") + "/" + file.name)
        resource_root = root / "resources"
        if resource_root.is_dir():
            for file in resource_root.rglob("*"):
                if file.is_file():
                    require(file.resolve().is_relative_to(repository), "Resource symlink escapes the repository")
                    resources.add(file.relative_to(resource_root).as_posix())
    return java, resources


def manifest_attributes(data):
    lines = data.decode("utf-8").replace("\r\n", "\n").split("\n")
    unfolded = []
    for line in lines:
        if not line:
            if unfolded:
                break  # only the main manifest section carries build identity
            continue
        if line.startswith(" "):
            require(bool(unfolded), "Invalid manifest continuation")
            unfolded[-1] += line[1:]
        else:
            unfolded.append(line)
    result = {}
    for line in unfolded:
        require(": " in line, "Invalid manifest header")
        key, value = line.split(": ", 1)
        require(key.lower() not in result, "Duplicate manifest header: %s" % key)
        result[key.lower()] = value
    return result


def verify(repository, jar, edition, version):
    require(edition in ("legit", "full"), "Unknown edition")
    repository = Path(repository).resolve()
    jar = Path(jar).resolve()
    display = "Legit" if edition == "legit" else "Full"
    name = "Alien Client " + display
    status = "Visuals & Performance" if edition == "legit" else "Architecture preview"
    config_directory = "alien-client" if edition == "legit" else "alien-client-full"
    require(jar.name == "AlienClient-%s-1.8.9.jar" % display, "Unexpected release JAR filename")
    require(jar.is_file() and jar.stat().st_size <= 512 * 1024 * 1024, "Missing or oversized JAR")
    allowed_sources, allowed_resources = source_inventory(repository, edition)
    all_full_sources, unused = source_inventory(repository, "full")
    legit_sources, unused = source_inventory(repository, "legit")
    forbidden_full_types = {path[:-5] for path in all_full_sources - legit_sources}
    parsed_classes = {}
    with zipfile.ZipFile(jar) as archive:
        entries = archive.infolist()
        names = [entry.filename for entry in entries]
        require(len(names) == len(set(names)), "Duplicate ZIP entries are forbidden")
        require(sum(entry.file_size for entry in entries) <= 1024 * 1024 * 1024, "Oversized expanded JAR")
        for entry in entries:
            path = PurePosixPath(entry.filename)
            require(not path.is_absolute() and ".." not in path.parts and "\\" not in entry.filename,
                    "Invalid archive path")
            require(not entry.flag_bits & 1, "Encrypted JAR entries are forbidden")
            if entry.is_dir():
                continue
            require(not entry.filename.endswith(".java"), "Source files must not ship in the binary JAR")
            if entry.filename.endswith(".class"):
                panelstudio_mit_classes = {
                    "com/lukflug/panelstudio/base/Animation.class",
                    "com/lukflug/panelstudio/base/SettingsAnimation.class",
                }
                require(entry.filename.startswith("dev/alien/client/") or entry.filename in panelstudio_mit_classes,
                        "Foreign/Minecraft classes bundled: %s" % entry.filename)
                require(not entry.filename.startswith("dev/alien/client/smoke/"), "Smoke classes bundled in production")
                parsed = classfile(archive.read(entry))
                require(entry.filename == parsed["name"] + ".class", "Classfile identity disagrees with its ZIP path")
                source_path = parsed["name"].rsplit("/", 1)[0] + "/" + parsed["source"]
                require(source_path in allowed_sources,
                        "Class has no source in the selected edition/common roots: %s (%s)" % (parsed["name"], source_path))
                if edition == "legit":
                    require(not parsed["name"].startswith("dev/alien/client/full/"), "Full class bundled in Legit")
                    for reference in parsed["references"]:
                        base = reference.lstrip("[").removeprefix("L").removesuffix(";").split("$", 1)[0]
                        require(not base.startswith("dev/alien/client/full/") and base not in forbidden_full_types,
                                "Legit class references a Full-only type: %s" % reference)
                    for full_type in forbidden_full_types:
                        require(not any(full_type in value or full_type.replace("/", ".") in value for value in parsed["strings"]),
                                "Legit constant pool names a Full-only source type: %s" % full_type)
                parsed_classes[parsed["name"]] = parsed
            else:
                require(entry.filename == "META-INF/MANIFEST.MF" or entry.filename in allowed_resources,
                        "Resource has no source in the selected edition/common roots: %s" % entry.filename)

        required_entries = {"META-INF/MANIFEST.MF", "META-INF/alienclient/edition.json", "mcmod.info"}
        require(required_entries.issubset(names), "Missing required manifest/edition marker/Forge metadata")
        manifest = manifest_attributes(archive.read("META-INF/MANIFEST.MF"))
        expected_headers = {"implementation-title": name, "implementation-version": version,
                            "alien-edition": edition, "alien-edition-status": status, "minecraft-version": "1.8.9"}
        for key, expected in expected_headers.items():
            require(manifest.get(key) == expected, "Manifest %s must be %r" % (key, expected))
        expected_marker = {"id": edition, "name": name, "allowFull": edition == "full",
                           "configDirectory": config_directory, "status": status}
        marker = strict_json(archive.read("META-INF/alienclient/edition.json"))
        require(isinstance(marker, dict) and type(marker.get("allowFull")) is bool and marker == expected_marker,
                "Packaged edition marker disagrees with the selected build")
        metadata = strict_json(archive.read("mcmod.info"))
        require(isinstance(metadata, list) and len(metadata) == 1, "Expected one Forge mod metadata entry")
        for key, expected in {"modid": "alienclient", "name": name, "version": version, "mcversion": "1.8.9"}.items():
            require(metadata[0].get(key) == expected, "Forge metadata %s disagrees with the selected build" % key)

    provider = parsed_classes.get("dev/alien/client/edition/BuildEdition")
    require(provider is not None, "Missing compiled edition provider")
    expected_fields = {"ID": ("Ljava/lang/String;", edition), "NAME": ("Ljava/lang/String;", name),
                       "ALLOW_FULL": ("Z", int(edition == "full")),
                       "CONFIG_DIRECTORY": ("Ljava/lang/String;", config_directory),
                       "STATUS": ("Ljava/lang/String;", status)}
    for key, expected in expected_fields.items():
        field = provider["fields"].get(key)
        require(field is not None and field[:2] == expected and field[2] & 0x0019 == 0x0019,
                "Compiled edition provider constant %s disagrees with the selected build" % key)
    entrypoint = parsed_classes.get("dev/alien/client/AlienClient")
    require(entrypoint is not None, "Missing Forge entry point")
    mod = entrypoint["annotations"].get("Lnet/minecraftforge/fml/common/Mod;")
    require(mod is not None, "Forge entry point has no runtime @Mod annotation")
    for key, expected in {"modid": "alienclient", "name": name, "version": version, "clientSideOnly": True,
                          "acceptedMinecraftVersions": "[1.8.9]"}.items():
        require(mod.get(key) == expected, "Compiled Forge @Mod %s disagrees with the selected build" % key)
    require("func_71410_x" in entrypoint["strings"], "Artifact is not ForgeGradle SRG-reobfuscated")
    require("getMinecraft" not in entrypoint["strings"], "Artifact still contains a deobfuscated Minecraft entry-point call")
    return {"status": "verified", "edition": edition, "version": version, "jar": str(jar),
            "classes": len(parsed_classes), "sha256": hashlib.sha256(jar.read_bytes()).hexdigest()}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repository", type=Path, default=Path(__file__).resolve().parent.parent)
    parser.add_argument("--edition", choices=("legit", "full"), required=True)
    parser.add_argument("--version", required=True)
    parser.add_argument("--jar", type=Path, required=True)
    args = parser.parse_args()
    try:
        print(json.dumps(verify(args.repository, args.jar, args.edition, args.version), sort_keys=True))
    except (InvalidArtifact, OSError, ValueError, TypeError, KeyError, IndexError, struct.error, zipfile.BadZipFile) as failure:
        print("Artifact verification failed: %s" % failure, file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
