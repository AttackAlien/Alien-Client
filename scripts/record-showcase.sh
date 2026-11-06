#!/usr/bin/env bash
# Continuous native X11 footage of the actual game; no interpolation or still-image input.
set -Eeuo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_dir"
out="$repo_dir/build/unified/showcase"
mkdir -p "$out/mesa-cache"
showcase_revision="$(git rev-parse HEAD)"
display="${ALIEN_SHOWCASE_DISPLAY:-:114}"
xorg_pid="";game_pid="";capture_pid=""
cleanup() {
  if [[ -n "$capture_pid" ]] && kill -0 "$capture_pid" 2>/dev/null; then kill -INT "$capture_pid" 2>/dev/null || true;wait "$capture_pid" 2>/dev/null || true;fi
  if [[ -n "$game_pid" ]] && kill -0 "$game_pid" 2>/dev/null; then kill -TERM -- "-$game_pid" 2>/dev/null || true;fi
  if [[ -n "$xorg_pid" ]]; then kill -TERM "$xorg_pid" 2>/dev/null || true;fi
}
trap cleanup EXIT
for tool in Xorg xdpyinfo ffmpeg ffprobe python3 setsid; do command -v "$tool" >/dev/null;done
rm -f "$out/ready.json" "$out/recording.start" "$out/capture.done" "$out/recording.stopped" "$out/failure.txt" "$out/result.json"
Xorg "$display" -config "$repo_dir/scripts/ci-combat-xorg.conf" -noreset -nolisten tcp -ac -logfile "$out/xorg.log" > "$out/xorg-console.txt" 2>&1 &
xorg_pid=$!
for _ in {1..50}; do if xdpyinfo -display "$display" >/dev/null 2>&1; then break;fi;sleep .2;done
xdpyinfo -display "$display" > "$out/xdpyinfo.txt"
setsid env DISPLAY="$display" LIBGL_ALWAYS_SOFTWARE=1 LP_NUM_THREADS=2 MESA_SHADER_CACHE_DIR="$out/mesa-cache" ./gradlew --offline --no-daemon -Pedition=unified "-PshowcaseSource=$showcase_revision" "-PshowcaseWidth=${ALIEN_SHOWCASE_WIDTH:-960}" "-PshowcaseHeight=${ALIEN_SHOWCASE_HEIGHT:-540}" "-PshowcaseFirst=${ALIEN_SHOWCASE_FIRST:-0}" "-PshowcaseLast=${ALIEN_SHOWCASE_LAST:-20}" runShowcaseClient > "$out/minecraft-console.txt" 2>&1 &
game_pid=$!
for _ in {1..900}; do
  if [[ -f "$out/ready.json" ]];then break;fi
  if [[ -f "$out/failure.txt" ]] || ! kill -0 "$game_pid" 2>/dev/null;then tail -n 60 "$out/minecraft-console.txt";exit 1;fi
  sleep .2
done
[[ -f "$out/ready.json" ]]
read -r movie_width movie_height movie_x movie_y < <(python3 -c 'import json,sys;r=json.load(open(sys.argv[1]));print(r["width"],r["height"],r["x"],r["y"])' "$out/ready.json")
ffmpeg -nostdin -hide_banner -loglevel warning -y -f x11grab -draw_mouse 0 -framerate 30 -video_size "${movie_width}x${movie_height}" -i "${display}+${movie_x},${movie_y}" -an -c:v libx264 -preset veryfast -crf 20 -threads 2 -pix_fmt yuv420p -movflags +faststart "$out/showcase-original.mp4" > "$out/ffmpeg-console.txt" 2>&1 &
capture_pid=$!
sleep .4
kill -0 "$capture_pid"
touch "$out/recording.start"
for _ in {1..1400}; do
  if [[ -f "$out/capture.done" ]];then break;fi
  if [[ -f "$out/failure.txt" ]] || ! kill -0 "$game_pid" 2>/dev/null;then cat "$out/failure.txt" 2>/dev/null || true;exit 1;fi
  if ! kill -0 "$capture_pid" 2>/dev/null;then cat "$out/ffmpeg-console.txt";exit 1;fi
  sleep .2
done
[[ -f "$out/capture.done" ]]
kill -INT "$capture_pid"
wait "$capture_pid" || true
capture_pid=""
touch "$out/recording.stopped"
wait "$game_pid"
game_pid=""
ffprobe -v error -show_format -show_streams -of json "$out/showcase-original.mp4" > "$out/ffprobe.json"
python3 - "$out" <<'PY'
from pathlib import Path
import hashlib,json,sys
from fractions import Fraction
p=Path(sys.argv[1]);r=json.loads((p/'result.json').read_text());probe=json.loads((p/'ffprobe.json').read_text());video=next(s for s in probe['streams'] if s['codec_type']=='video')
assert r['status']=='PASS' and len(r['chapters'])>=1
encoded_fps=float(Fraction(video['avg_frame_rate']))
assert video['codec_name']=='h264' and 29.9<=encoded_fps<=30.1 and video['pix_fmt']=='yuv420p'
assert float(probe['format']['duration'])>=sum(c['plannedSeconds'] for c in r['chapters'])-1
assert r['medianRenderedFps']>=28,(r['medianRenderedFps'],r['p95FrameMs'])
assert all(c['medianRenderedFps']>=28 for c in r['chapters']),[(c['title'],c['medianRenderedFps']) for c in r['chapters'] if c['medianRenderedFps']<28]
proof={'sourceCommit':r['sourceCommit'],'captureMethod':'Continuous native X11 / ffmpeg x11grab','generatedFrames':False,'interpolatedFrames':False,'requestedRecordingFps':30,'recordingFps':encoded_fps,'encodedRateRational':video['avg_frame_rate'],'medianRenderedFps':r['medianRenderedFps'],'p95FrameMs':r['p95FrameMs'],'minimumChapterMedianRenderedFps':min(c['medianRenderedFps'] for c in r['chapters']),'chapters':r['chapters'],'duration':float(probe['format']['duration']),'width':video['width'],'height':video['height'],'sha256':hashlib.sha256((p/'showcase-original.mp4').read_bytes()).hexdigest(),'bytes':(p/'showcase-original.mp4').stat().st_size}
(p/'video-validation.json').write_text(json.dumps(proof,indent=2)+'\n');print(json.dumps(proof,indent=2))
PY
