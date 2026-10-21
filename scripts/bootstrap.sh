#!/usr/bin/env bash
# Download only official distributions and verify pinned SHA-256 digests.
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
tool_dir="${ALIEN_TOOLS_DIR:-$repo_dir/.tools}"
mkdir -p "$tool_dir"
download_verified() {
  local url="$1" file="$2" digest="$3"
  if [[ ! -f "$file" ]] || ! printf '%s  %s\n' "$digest" "$file" | sha256sum --check --status; then
    curl --fail --location --retry 2 --connect-timeout 20 --output "$file.part" "$url"
    printf '%s  %s\n' "$digest" "$file.part" | sha256sum --check --status
    mv "$file.part" "$file"
  fi
  printf '%s  %s\n' "$digest" "$file" | sha256sum --check --status
}
jdk_archive="$tool_dir/OpenJDK8U-jdk_x64_linux_hotspot_8u472b08.tar.gz"
gradle_archive="$tool_dir/gradle-2.14.1-bin.zip"
download_verified 'https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u472-b08/OpenJDK8U-jdk_x64_linux_hotspot_8u472b08.tar.gz' "$jdk_archive" '5becaa4ac660e844c5a39e2ebc39ff5ac824c37ff1b625af8c8b111dc13c3592'
download_verified 'https://services.gradle.org/distributions/gradle-2.14.1-bin.zip' "$gradle_archive" 'cfc61eda71f2d12a572822644ce13d2919407595c2aec3e3566d2aab6f97ef39'
if [[ ! -x "$tool_dir/jdk8u472-b08/bin/javac" ]]; then
  tar -xzf "$jdk_archive" -C "$tool_dir"
fi
if [[ ! -f "$tool_dir/gradle-2.14.1/lib/gradle-launcher-2.14.1.jar" ]]; then
  unzip -q "$gradle_archive" -d "$tool_dir"
fi
chmod +x "$tool_dir/gradle-2.14.1/bin/gradle"
"$tool_dir/jdk8u472-b08/bin/java" -version
printf 'Toolchain ready in %s\n' "$tool_dir"
