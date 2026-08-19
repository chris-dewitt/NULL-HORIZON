#!/usr/bin/env bash
# Build a Play-ready Android App Bundle (AAB).
# Requires a release keystore configured via android-app/keystore.properties
# (see keystore.properties.example) or PLAY_STORE_* environment variables.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="${ROOT}/android-app"
OUT_DIR="${ROOT}/dist/play"
VERSION_NAME="${PLAY_VERSION_NAME:-1.0.0}"
VERSION_CODE="${PLAY_VERSION_CODE:-1}"

mkdir -p "${OUT_DIR}"

if [[ ! -f "${APP_DIR}/keystore.properties" && -z "${PLAY_STORE_FILE:-}" ]]; then
  cat <<EOF
Missing release signing config.

Create an upload keystore (once):
  mkdir -p ${APP_DIR}/keystore
  keytool -genkeypair -v \\
    -keystore ${APP_DIR}/keystore/nullhorizon-upload.jks \\
    -alias nullhorizon \\
    -keyalg RSA -keysize 2048 -validity 10000

Then copy ${APP_DIR}/keystore.properties.example to
${APP_DIR}/keystore.properties and fill in passwords.

Or export PLAY_STORE_FILE, PLAY_STORE_PASSWORD, PLAY_KEY_ALIAS, PLAY_KEY_PASSWORD.
EOF
  exit 1
fi

echo "Syncing content bundle into Android assets…"
python3 "${ROOT}/scripts/build_bundle.py" --channel prod --sync-android-assets

export PLAY_VERSION_NAME="${VERSION_NAME}"
export PLAY_VERSION_CODE="${VERSION_CODE}"

echo "Building signed release bundle (versionName=${VERSION_NAME} versionCode=${VERSION_CODE})…"
(
  cd "${APP_DIR}"
  ./gradlew :app:bundleRelease --no-daemon
)

AAB="$(find "${APP_DIR}/app/build/outputs/bundle/release" -name '*.aab' | head -n 1)"
if [[ -z "${AAB}" ]]; then
  echo "No AAB produced under app/build/outputs/bundle/release"
  exit 1
fi

DEST="${OUT_DIR}/nullhorizon-${VERSION_NAME}-${VERSION_CODE}.aab"
cp "${AAB}" "${DEST}"
echo "Wrote ${DEST}"
echo
echo "Next: follow docs/release/PLAY_PUBLISH.md to upload in Play Console."
