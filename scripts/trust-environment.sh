#!/usr/bin/env bash
# Build a local JKS using the verified JDK roots plus OS-provided public CAs.
# This preserves TLS hostname and certificate verification behind the cloud proxy.
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
tool_dir="${ALIEN_TOOLS_DIR:-$repo_dir/.tools}"
jdk_dir="${JAVA_HOME:-$tool_dir/jdk8u472-b08}"
trust_dir="$tool_dir/trust"
mkdir -p "$trust_dir"
shopt -s nullglob
certs=(/usr/local/share/ca-certificates/*.crt)
fingerprint="$(sha256sum "$jdk_dir/jre/lib/security/cacerts" "${certs[@]}" | sha256sum | cut -d' ' -f1)"
if [[ ! -f "$trust_dir/cacerts.jks" ]] || [[ ! -f "$trust_dir/source.sha256" ]] || [[ "$(cat "$trust_dir/source.sha256")" != "$fingerprint" ]]; then
  cp "$jdk_dir/jre/lib/security/cacerts" "$trust_dir/cacerts.jks.next"
  for cert in "${certs[@]}"; do
    "$jdk_dir/bin/keytool" -importcert -noprompt -trustcacerts -alias "alien-environment-$(basename "$cert")" -file "$cert" -keystore "$trust_dir/cacerts.jks.next" -storepass changeit >/dev/null 2>&1
  done
  mv "$trust_dir/cacerts.jks.next" "$trust_dir/cacerts.jks"
  printf '%s\n' "$fingerprint" > "$trust_dir/source.sha256"
fi
