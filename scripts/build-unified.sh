#!/usr/bin/env bash
# One installable Alien Client JAR with per-profile combat settings.
# The output is separate from historical two-edition release folders.
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
bash ./scripts/bootstrap.sh
./gradlew --no-daemon -Pedition=unified setupDecompWorkspace build
jar="$repo_dir/build/unified/libs/AlienClient-Unified-1.8.9.jar"
test -s "$jar"
distribution="$repo_dir/build/unified/distribution"
mkdir -p "$distribution"
cp "$jar" "$distribution/AlienClient-Unified-1.8.9.jar"
(cd "$distribution" && sha256sum AlienClient-Unified-1.8.9.jar > SHA256SUMS.txt)
echo "One installable file: $distribution/AlienClient-Unified-1.8.9.jar"
echo "Do not install Unified alongside the old Legit or Full JARs."
