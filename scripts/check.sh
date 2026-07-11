#!/usr/bin/env bash
# Repository validation entrypoint for NULL HORIZON.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

FAIL=0
RAN_ANY=0

section() {
  printf '\n==> %s\n' "$1"
}

run_backend() {
  section "Backend lint, types, and tests"
  if [[ ! -d "$ROOT_DIR/backend/.venv" ]]; then
    echo "backend/.venv missing; run ./scripts/bootstrap.sh first" >&2
    FAIL=1
    return
  fi
  # shellcheck disable=SC1091
  source "$ROOT_DIR/backend/.venv/bin/activate"
  (
    cd "$ROOT_DIR/backend"
    ruff check .
    ruff format --check .
    mypy api
    pytest -q
  )
  RAN_ANY=1
}

run_android() {
  section "Android unit tests and debug assemble"
  if [[ -z "${ANDROID_HOME:-}${ANDROID_SDK_ROOT:-}" && ! -f "$ROOT_DIR/android-app/local.properties" ]]; then
    echo "Skipping Android checks: ANDROID_HOME/ANDROID_SDK_ROOT unset and local.properties missing"
    return
  fi
  if [[ ! -x "$ROOT_DIR/android-app/gradlew" ]]; then
    echo "android-app/gradlew is missing" >&2
    FAIL=1
    return
  fi
  (
    cd "$ROOT_DIR/android-app"
    ./gradlew --no-daemon lintDebug test assembleDebug
  )
  RAN_ANY=1
}

run_secret_scan() {
  section "Basic secret scan"
  if git grep -nE 'BEGIN (RSA |OPENSSH |EC )?PRIVATE KEY|AKIA[0-9A-Z]{16}|xox[baprs]-[0-9A-Za-z-]{10,}' -- \
    ':(exclude).env.example' \
    ':(exclude)**/*.md' \
    >/tmp/null-horizon-secret-scan.txt; then
    echo "Potential secrets detected:" >&2
    cat /tmp/null-horizon-secret-scan.txt >&2
    FAIL=1
  else
    echo "No obvious secrets matched"
  fi
  RAN_ANY=1
}

run_structure_checks() {
  section "Monorepo structure checks"
  required_paths=(
    AGENTS.md
    docs/PRODUCT_SPEC.md
    docs/ADR/0000-template.md
    backend/api/app/main.py
    android-app/app/src/main/java/com/nullhorizon/app/MainActivity.kt
    .github/workflows/ci.yml
  )
  for path in "${required_paths[@]}"; do
    if [[ ! -e "$ROOT_DIR/$path" ]]; then
      echo "Missing required path: $path" >&2
      FAIL=1
    fi
  done
  echo "Required paths present"
  RAN_ANY=1
}

run_structure_checks
run_backend
run_android
run_secret_scan

if (( FAIL != 0 )); then
  echo
  echo "check.sh failed"
  exit 1
fi

if (( RAN_ANY == 0 )); then
  echo "No checks ran" >&2
  exit 1
fi

echo
echo "All available checks passed"
