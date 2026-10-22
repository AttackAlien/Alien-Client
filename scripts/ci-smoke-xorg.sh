#!/usr/bin/env bash
# Headless CI integration test using a connected Xorg dummy display.
# Xvfb's disconnected RandR screen crashes LWJGL 2 before Minecraft starts.
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
edition="${ALIEN_EDITION:?ALIEN_EDITION must be legit or full}"
case "$edition" in legit|full) ;; *) echo "Unexpected edition: $edition" >&2; exit 1 ;; esac
smoke_dir="$repo_dir/build/$edition/smoke"
mkdir -p "$smoke_dir"
display="${ALIEN_SMOKE_DISPLAY:-:99}"
command -v Xorg >/dev/null || { echo "Xorg is required for CI." >&2; exit 1; }
command -v xdpyinfo >/dev/null || { echo "xdpyinfo is required for display diagnostics." >&2; exit 1; }

# The GitHub runner is headless; launching Xorg as root avoids Xorg.wrap
# restrictions. Display is bound to local Unix sockets only (no TCP).
sudo Xorg "$display" -config "$repo_dir/scripts/ci-xorg-dummy.conf" -noreset \
  -nolisten tcp -ac -logfile "$smoke_dir/xorg.log" \
  >"$smoke_dir/xorg.stdout.log" 2>&1 &
xorg_pid=$!
finish() {
  sudo kill "$xorg_pid" >/dev/null 2>&1 || true
  wait "$xorg_pid" 2>/dev/null || true
}
trap finish EXIT
export DISPLAY="$display"
export LIBGL_ALWAYS_SOFTWARE=1

for i in {1..60}; do
  if xdpyinfo -display "$display" >/dev/null 2>&1; then break; fi
  if ! kill -0 "$xorg_pid" >/dev/null 2>&1; then
    echo "Xorg exited unexpectedly." >&2
    cat "$smoke_dir/xorg.stdout.log" >&2
    exit 1
  fi
  sleep .25
done
xdpyinfo -display "$display" > "$smoke_dir/xdpyinfo.txt"
grep -q 'dimensions:.*1280x720' "$smoke_dir/xdpyinfo.txt"
# Both extensions are supported by the dummy server in the verified setup.
grep -q 'RANDR' "$smoke_dir/xdpyinfo.txt"
grep -q 'XFree86-VidModeExtension' "$smoke_dir/xdpyinfo.txt"
xrandr --query | tee "$smoke_dir/xrandr.txt"
glxinfo -B | tee "$smoke_dir/glxinfo.txt"

ALIEN_EDITION="$edition" ./scripts/smoke-client.sh
