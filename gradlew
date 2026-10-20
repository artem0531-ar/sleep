#!/bin/sh
set -eu
cd "$(dirname "$0")"
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
VERSION=7.6.4
DIST="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/sleeptrack-gradle-$VERSION"
BIN="$DIST/gradle-$VERSION/bin/gradle"
if [ ! -f "$BIN" ]; then
  mkdir -p "$DIST"
  ZIP="$DIST/gradle-$VERSION-bin.zip"
  URL="https://services.gradle.org/distributions/gradle-$VERSION-bin.zip"
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 "$URL" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "$URL"
  else
    echo 'Gradle is not installed, and neither curl nor wget is available.' >&2
    exit 1
  fi
  unzip -oq "$ZIP" -d "$DIST"
  rm -f "$ZIP"
fi
exec sh "$BIN" "$@"
