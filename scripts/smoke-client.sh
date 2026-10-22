#!/usr/bin/env bash
# Runs a disposable actual Minecraft/Forge session with the test-only mod.
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
tool_dir="${ALIEN_TOOLS_DIR:-$repo_dir/.tools}"
edition="${ALIEN_EDITION:-legit}"
case "$edition" in legit|full) ;; *) printf 'ALIEN_EDITION must be legit or full\n' >&2; exit 1 ;; esac
smoke_dir="build/$edition/smoke"
mkdir -p "$smoke_dir"
rm -f "$smoke_dir/result.txt"
verify_result() {
  [[ -s "$smoke_dir/result.txt" ]] || return 1
  local summary line
  {
    IFS= read -r summary
    [[ "$summary" =~ ^PASS\ [1-9][0-9]*\ checks$ ]] || return 1
    while IFS= read -r line; do
      [[ "$line" != FAIL* ]] || return 1
    done
  } < "$smoke_dir/result.txt"
  printf '%s\n' "$summary"
}
if [[ -n "${DISPLAY:-}" ]]; then
  ./gradlew --no-daemon "-Pedition=$edition" runSmokeClient
  verify_result
  exit 0
fi
"$repo_dir/scripts/setup-smoke-display.sh"
if command -v Xvfb >/dev/null 2>&1; then
  display_binary="$(command -v Xvfb)"
else
  display_binary="$tool_dir/xvfb/root/usr/bin/Xvfb"
fi
display_number="${ALIEN_SMOKE_DISPLAY:-98}"
export DISPLAY=":$display_number"
export LIBGL_ALWAYS_SOFTWARE=1
"$display_binary" "$DISPLAY" -screen 0 1280x720x24 +extension GLX +render -noreset -nolisten tcp -ac > "$smoke_dir/xvfb.log" 2>&1 &
display_pid=$!
trap 'kill "$display_pid" 2>/dev/null || true; wait "$display_pid" 2>/dev/null || true' EXIT
for attempt in {1..30}; do
  if ! kill -0 "$display_pid" 2>/dev/null; then cat "$smoke_dir/xvfb.log" >&2; exit 1; fi
  if [[ -S "/tmp/.X11-unix/X$display_number" ]]; then break; fi
  sleep 0.1
done
./gradlew --no-daemon "-Pedition=$edition" runSmokeClient
verify_result
