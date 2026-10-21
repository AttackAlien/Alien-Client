#!/usr/bin/env bash
# Optional Debian cloud display for real integration tests; no system install.
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
tool_dir="${ALIEN_TOOLS_DIR:-$repo_dir/.tools}"
display_binary="$tool_dir/xvfb/root/usr/bin/Xvfb"
if command -v Xvfb >/dev/null 2>&1 || [[ -x "$display_binary" ]]; then
  exit 0
fi
if [[ ! -x /usr/bin/apt-get ]] || [[ ! -f /usr/share/keyrings/debian-archive-keyring.gpg ]]; then
  printf 'Install Xvfb using your OS package manager for headless integration tests.\n' >&2
  exit 1
fi
source /etc/os-release
if [[ "${ID:-}" != debian ]] || [[ "${VERSION_CODENAME:-}" != trixie ]]; then
  printf 'Automatic userland Xvfb setup currently supports Debian trixie only.\n' >&2
  exit 1
fi
apt_dir="$tool_dir/apt"
mkdir -p "$apt_dir/lists/partial" "$apt_dir/cache/archives/partial" "$apt_dir/conf.d" "$tool_dir/xvfb"
printf 'deb [signed-by=/usr/share/keyrings/debian-archive-keyring.gpg] https://deb.debian.org/debian trixie main\n' > "$apt_dir/sources.list"
cat > "$apt_dir/apt.conf" <<EOF
Dir::Etc::sourcelist "$apt_dir/sources.list";
Dir::Etc::sourceparts "-";
Dir::Etc::parts "$apt_dir/conf.d";
Dir::Etc::main "-";
Dir::State::lists "$apt_dir/lists";
Dir::Cache "$apt_dir/cache";
APT::Update::Error-Mode "any";
EOF
proxy_options=()
proxy_url="${HTTPS_PROXY:-${https_proxy:-}}"
if [[ -n "$proxy_url" ]]; then
  proxy_authority="${proxy_url#*://}"
  proxy_authority="${proxy_authority##*@}"
  proxy_authority="${proxy_authority%%/*}"
  proxy_options=(-o "Acquire::https::Proxy=http://$proxy_authority")
fi
APT_CONFIG="$apt_dir/apt.conf" /usr/bin/apt-get "${proxy_options[@]}" update
(
  cd "$tool_dir/xvfb"
  # APT verifies packages against the SHA-256 in the signed Debian index.
  APT_CONFIG="$apt_dir/apt.conf" /usr/bin/apt-get "${proxy_options[@]}" download xvfb
  for package in xvfb_*.deb; do dpkg-deb -x "$package" root; done
)
if ldd "$display_binary" | grep -q 'not found'; then
  printf 'Xvfb requires additional OS libraries; see ldd output.\n' >&2
  ldd "$display_binary" >&2
  exit 1
fi
