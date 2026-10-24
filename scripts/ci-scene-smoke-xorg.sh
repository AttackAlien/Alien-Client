#!/usr/bin/env bash
# Run a disposable Forge 1.8.9 real-render smoke inside a connected Xorg dummy monitor.
# The smoke either writes PASS with framebuffer images, or fails with bounded diagnostics.
set -Eeuo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
edition="${ALIEN_EDITION:?ALIEN_EDITION must be legit or full}"
case "$edition" in unified) ;; *) echo "This runner requires the Unified build, got: $edition" >&2; exit 2;; esac
out="$repo_dir/build/$edition/scene-smoke"
mkdir -p "$out"
display="${ALIEN_SCENE_SMOKE_DISPLAY:-:103}"
deadline="${ALIEN_SCENE_SMOKE_TIMEOUT_SECONDS:-480}"
if ! [[ "$deadline" =~ ^[1-9][0-9]*$ ]] || (( deadline>900 )); then echo "Invalid timeout" >&2; exit 2; fi

report() {
  echo "$(date -u +'%Y-%m-%dT%H:%M:%SZ') | $*" | tee -a "$out/ci-stages.txt"
}
collect() {
  report "Capturing Xorg/Minecraft diagnostics"
  ps -eo pid,ppid,etime,stat,args > "$out/processes.txt" || true
  local game="$repo_dir/build/$edition/scene-smoke-runtime"
  if [[ -f "$game/logs/latest.log" ]]; then
    cp "$game/logs/latest.log" "$out/minecraft-latest.log" || true
    tail -n 75 "$game/logs/latest.log" || true
  fi
  if [[ -d "$game/crash-reports" ]]; then
    mkdir -p "$out/crash-reports"
    cp "$game/crash-reports/"*.txt "$out/crash-reports/" 2>/dev/null || true
  fi
  if [[ -f "$out/minecraft-console.log" ]]; then tail -n 75 "$out/minecraft-console.log" || true; fi
  if [[ -f "$out/xorg.stdout.log" ]]; then tail -n 50 "$out/xorg.stdout.log" || true; fi
}
cleanup() {
  if [[ -n "${xorg_pid:-}" ]]; then
    timeout 4s sudo -n pkill -TERM -P "$xorg_pid" >/dev/null 2>&1 || true
    timeout 4s sudo -n kill "$xorg_pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT
for tool in Xorg xdpyinfo xrandr glxinfo timeout; do
  command -v "$tool" >/dev/null || { report "Missing $tool"; exit 1; }
done
report "Launching isolated connected Xorg dummy screen $display"
sudo -n Xorg "$display" -config "$repo_dir/scripts/ci-combat-xorg.conf" -noreset \
  -nolisten tcp -ac -logfile "$out/xorg.log" > "$out/xorg.stdout.log" 2>&1 &
xorg_pid=$!
export DISPLAY="$display"
export LIBGL_ALWAYS_SOFTWARE=1

ready=false
for i in {1..45}; do
  if timeout 2s xdpyinfo -display "$display" >/dev/null 2>&1; then ready=true;break; fi
  if ! kill -0 "$xorg_pid" >/dev/null 2>&1; then break;fi
  sleep .2
done
if [[ "$ready" != true ]]; then report "Xorg failed to become ready";collect;exit 1;fi
timeout 15s xdpyinfo > "$out/xdpyinfo.txt"
grep -q 'dimensions:.*1280x720' "$out/xdpyinfo.txt" || { collect;exit 1; }
grep -q 'RANDR' "$out/xdpyinfo.txt" || { collect;exit 1; }
grep -q 'XFree86-VidModeExtension' "$out/xdpyinfo.txt" || { collect;exit 1; }
timeout 15s xrandr --query > "$out/xrandr.txt" || { collect;exit 1; }
timeout 20s glxinfo -B > "$out/glxinfo.txt" || { collect;exit 1; }
cat "$out/glxinfo.txt"
report "Running actual Minecraft, edition=$edition, deadline=${deadline}s"
rm -f "$out/result.txt"
set +e
timeout --signal=QUIT --kill-after=15s "${deadline}s" \
  ./gradlew --no-daemon "-Pedition=$edition" runSceneSmokeClient \
  2>&1 | tee "$out/minecraft-console.log"
status=${PIPESTATUS[0]}
set -e
if ((status!=0)); then report "Smoke failed (status $status; 124 means timed out)";collect;exit 1;fi
if [[ ! -s "$out/result.txt" ]] || ! head -n 1 "$out/result.txt" | grep -Eq '^PASS [1-9][0-9]* checks$'; then
  report "Smoke missing PASS marker";collect;exit 1
fi
report "PASS: $(head -n 1 "$out/result.txt")"
