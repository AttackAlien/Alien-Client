#!/usr/bin/env bash
# Source this script to initialize the pinned JDK and standard proxy support.
alien_repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
alien_tool_dir="${ALIEN_TOOLS_DIR:-$alien_repo_dir/.tools}"
if [[ -x "$alien_tool_dir/jdk8u472-b08/bin/javac" ]]; then
  export JAVA_HOME="$alien_tool_dir/jdk8u472-b08"
fi
if [[ -z "${JAVA_HOME:-}" ]] || [[ ! -x "$JAVA_HOME/bin/javac" ]]; then
  printf 'Java 8 JDK is required. Run scripts/bootstrap.sh or set JAVA_HOME.\n' >&2
  return 1
fi
if ! "$JAVA_HOME/bin/java" -version 2>&1 | head -1 | grep -q '"1\.8\.'; then
  printf 'This ForgeGradle version requires Java 8.\n' >&2
  return 1
fi
export PATH="$JAVA_HOME/bin:$PATH"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$alien_repo_dir/.gradle-user}"
# Java/Gradle do not consume HTTPS_PROXY automatically. Forward the hostname
# and port only: never log or copy credentials from the proxy URL.
alien_proxy="${HTTPS_PROXY:-${https_proxy:-}}"
if [[ -n "$alien_proxy" ]]; then
  alien_proxy_authority="${alien_proxy#*://}"
  alien_proxy_authority="${alien_proxy_authority##*@}"
  alien_proxy_authority="${alien_proxy_authority%%/*}"
  alien_proxy_host="${alien_proxy_authority%%:*}"
  alien_proxy_port="${alien_proxy_authority##*:}"
  [[ "$alien_proxy_port" != "$alien_proxy_host" ]] || alien_proxy_port=80
  export GRADLE_OPTS="${GRADLE_OPTS:-} -Dhttps.proxyHost=$alien_proxy_host -Dhttps.proxyPort=$alien_proxy_port -Dhttp.proxyHost=$alien_proxy_host -Dhttp.proxyPort=$alien_proxy_port"
fi
ALIEN_TOOLS_DIR="$alien_tool_dir" "$alien_repo_dir/scripts/trust-environment.sh"
export GRADLE_OPTS="${GRADLE_OPTS:-} -Djavax.net.ssl.trustStore=$alien_tool_dir/trust/cacerts.jks -Djavax.net.ssl.trustStorePassword=changeit"
