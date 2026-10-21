#!/usr/bin/env bash
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$repo_dir/scripts/gradle-env.sh"
if [[ -f "$alien_tool_dir/gradle-2.14.1/lib/gradle-launcher-2.14.1.jar" ]]; then
  exec "$JAVA_HOME/bin/java" ${GRADLE_OPTS:-} -classpath "$alien_tool_dir/gradle-2.14.1/lib/gradle-launcher-2.14.1.jar" org.gradle.launcher.GradleMain "$@"
fi
exec "$JAVA_HOME/bin/java" ${GRADLE_OPTS:-} -classpath "$repo_dir/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
