#!/usr/bin/env bash
# Bootstrap local development dependencies for NULL HORIZON.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> Bootstrapping NULL HORIZON"

if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is required" >&2
  exit 1
fi

PYTHON_VERSION="$(python3 -c 'import sys; print(f"{sys.version_info.major}.{sys.version_info.minor}")')"
REQUIRED_MAJOR=3
REQUIRED_MINOR=12
ACTUAL_MAJOR="$(echo "$PYTHON_VERSION" | cut -d. -f1)"
ACTUAL_MINOR="$(echo "$PYTHON_VERSION" | cut -d. -f2)"
if (( ACTUAL_MAJOR < REQUIRED_MAJOR || (ACTUAL_MAJOR == REQUIRED_MAJOR && ACTUAL_MINOR < REQUIRED_MINOR) )); then
  echo "Python 3.12+ is required (found $PYTHON_VERSION)" >&2
  exit 1
fi

echo "==> Creating backend virtualenv"
python3 -m venv "$ROOT_DIR/backend/.venv"
# shellcheck disable=SC1091
source "$ROOT_DIR/backend/.venv/bin/activate"
python -m pip install --upgrade pip
python -m pip install -e "$ROOT_DIR/backend[dev]"

if [[ ! -f "$ROOT_DIR/.env" && -f "$ROOT_DIR/.env.example" ]]; then
  cp "$ROOT_DIR/.env.example" "$ROOT_DIR/.env"
  echo "==> Created .env from .env.example (no secrets included)"
fi

if [[ -n "${ANDROID_HOME:-}${ANDROID_SDK_ROOT:-}" ]]; then
  echo "==> Android SDK detected"
else
  echo "==> Android SDK not detected; Android builds will be skipped until ANDROID_HOME is set"
fi

echo "==> Bootstrap complete. Run ./scripts/check.sh"
