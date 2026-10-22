#!/usr/bin/env bash
# Each process gets one source root and independent ForgeGradle output paths.
# Release copies appear only after both editions pass build, binary inspection,
# and the real Minecraft smoke workflow.
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
for edition in legit full; do
  ./gradlew --no-daemon "-Pedition=$edition" clean build
  ALIEN_EDITION="$edition" ./scripts/smoke-client.sh
done
release_dir="$repo_dir/build/releases"
mkdir -p "$release_dir"
cp build/legit/libs/AlienClient-Legit-1.8.9.jar "$release_dir/AlienClient-Legit-1.8.9.jar"
cp build/full/libs/AlienClient-Full-1.8.9.jar "$release_dir/AlienClient-Full-1.8.9.jar"
sha256sum "$release_dir/AlienClient-Legit-1.8.9.jar" "$release_dir/AlienClient-Full-1.8.9.jar"
