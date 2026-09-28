#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export ANDROID_HOME="${ANDROID_HOME:-$ROOT_DIR/.android-sdk}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"

if [[ ! -d "$ANDROID_HOME/platforms/android-36" ]]; then
  echo "Android SDK platform 36 not found at $ANDROID_HOME. Follow the Replit setup steps in replit.md." >&2
  exit 1
fi

exec "$ROOT_DIR/android/gradlew" \
  --no-daemon \
  --project-dir "$ROOT_DIR/android" \
  test assembleDebug