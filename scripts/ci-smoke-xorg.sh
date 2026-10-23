#!/usr/bin/env bash
# Bounded CI smoke: a connected Xorg dummy display for LWJGL 2 / Forge 1.8.9.
# This test MUST produce a PASS result, fail with diagnostics, or time out.
set -Eeuo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
edition="${ALIEN_EDITION:?ALIEN_EDITION must be legit, full or unified}"
case "$edition" in legit|full|unified) ;; *) echo "Unexpected edition: $edition" >&2; exit 1 ;; esac
smoke_dir="$repo_dir/build/$edition/smoke"
mkdir -p "$smoke_dir"
display="${ALIEN_SMOKE_DISPLAY:-:99}"
timeout_seconds="${ALIEN_SMOKE_TIMEOUT_SECONDS:-420}"
if ! [[ "$timeout_seconds" =~ ^[1-9][0-9]*$ ]] || ((timeout_seconds>900)); then
  echo "Invalid smoke deadline (seconds): $timeout_seconds" >&2; exit 2
fi

stage() {
  printf '%s - %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$*" | tee -a "$smoke_dir/ci-stages.log"
}
diagnose() {
  stage "Collecting environment and Minecraft diagnostics"
  ps -eo pid,ppid,stat,etime,args > "$smoke_dir/processes.txt" || true
  if [[ -d "$repo_dir/build/$edition/smoke-runtime/crash-reports" ]]; then
    mkdir -p "$smoke_dir/crash-reports"
    cp "$repo_dir/build/$edition/smoke-runtime/crash-reports/"*.txt "$smoke_dir/crash-reports/" 2>/dev/null || true
  fi
  local game_log="$repo_dir/build/$edition/smoke-runtime/logs/latest.log"
  if [[ -f "$game_log" ]]; then
    cp "$game_log" "$smoke_dir/minecraft-latest.log" || true
    tail -n 80 "$game_log" || true
  fi
  if [[ -f "$smoke_dir/xorg.stdout.log" ]]; then tail -n 60 "$smoke_dir/xorg.stdout.log" || true; fi
  if [[ -f "$smoke_dir/minecraft-console.log" ]]; then tail -n 100 "$smoke_dir/minecraft-console.log" || true; fi
}
cleanup() {
  # Do not hang CI while waiting for a privileged X server to stop.
  if [[ -n "${xorg_pid:-}" ]]; then
    timeout 5s sudo -n pkill -TERM -P "$xorg_pid" >/dev/null 2>&1 || true
    timeout 5s sudo -n kill "$xorg_pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT
command -v Xorg >/dev/null || { echo "Missing Xorg" >&2; exit 1; }
command -v xdpyinfo >/dev/null || { echo "Missing xdpyinfo" >&2; exit 1; }
command -v xrandr >/dev/null || { echo "Missing xrandr" >&2; exit 1; }
command -v glxinfo >/dev/null || { echo "Missing glxinfo" >&2; exit 1; }
command -v timeout >/dev/null || { echo "Missing GNU timeout" >&2; exit 1; }

stage "Starting local Xorg dummy display $display"
sudo -n Xorg "$display" -config "$repo_dir/scripts/ci-xorg-dummy.conf" -noreset \
  -nolisten tcp -ac -logfile "$smoke_dir/xorg.log" \
  >"$smoke_dir/xorg.stdout.log" 2>&1 &
xorg_pid=$!
export DISPLAY="$display"
export LIBGL_ALWAYS_SOFTWARE=1

ready=false
for i in {1..60}; do
  if timeout 2s xdpyinfo -display "$display" >/dev/null 2>&1; then ready=true; break; fi
  if ! kill -0 "$xorg_pid" >/dev/null 2>&1; then break; fi
  sleep .2
done
if [[ "$ready" != true ]]; then
  stage "ERROR: Xorg did not become ready within the display deadline"
  diagnose
  exit 1
fi
stage "Checking GLX, connected monitor and display modes"
timeout 15s xdpyinfo -display "$display" > "$smoke_dir/xdpyinfo.txt"
grep -q 'dimensions:.*1280x720' "$smoke_dir/xdpyinfo.txt" || { diagnose; exit 1; }
grep -q 'RANDR' "$smoke_dir/xdpyinfo.txt" || { diagnose; exit 1; }
grep -q 'XFree86-VidModeExtension' "$smoke_dir/xdpyinfo.txt" || { diagnose; exit 1; }
if ! timeout 15s xrandr --query > "$smoke_dir/xrandr.txt" 2>&1; then diagnose; exit 1; fi
cat "$smoke_dir/xrandr.txt"
if ! timeout 20s glxinfo -B > "$smoke_dir/glxinfo.txt" 2>&1; then
  stage "ERROR: OpenGL initialization failed or timed out"
  diagnose
  exit 1
fi
cat "$smoke_dir/glxinfo.txt"

stage "Running Minecraft/Forge smoke (hard deadline: ${timeout_seconds}s)"
# QUIT causes Java 8 to print a thread dump before the process group is
# forcibly killed; this helps identify render or shutdown deadlocks.
set +e
timeout --signal=QUIT --kill-after=15s "${timeout_seconds}s" \
  env ALIEN_EDITION="$edition" bash "$repo_dir/scripts/smoke-client.sh" \
  2>&1 | tee "$smoke_dir/minecraft-console.log"
smoke_status=${PIPESTATUS[0]}
set -e
if (( smoke_status != 0 )); then
  stage "ERROR: Minecraft smoke exited with status $smoke_status (124/137 = deadline)"
  diagnose
  exit 1
fi
if ! [[ -s "$smoke_dir/result.txt" ]] || ! head -n 1 "$smoke_dir/result.txt" | grep -Eq '^PASS [1-9][0-9]* checks$'; then
  stage "ERROR: Smoke did not produce a valid PASS marker"
  diagnose
  exit 1
fi
stage "PASS: $(head -n 1 "$smoke_dir/result.txt")"
