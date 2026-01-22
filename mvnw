#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper startup script, POSIX.
# ----------------------------------------------------------------------------

set -euf

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_PROPERTIES" ]; then
  echo "[ERROR] $WRAPPER_PROPERTIES not found." >&2
  exit 1
fi

WRAPPER_URL=$(grep -E '^wrapperUrl=' "$WRAPPER_PROPERTIES" | sed 's/wrapperUrl=//') || true
if [ -z "${WRAPPER_URL:-}" ]; then
  WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$BASE_DIR/.mvn/wrapper"
  echo "Downloading Maven Wrapper..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "[ERROR] curl/wget not found to download wrapper." >&2
    exit 1
  fi
fi

MAVEN_OPTS="${MAVEN_OPTS:-} -Dmaven.multiModuleProjectDirectory=$BASE_DIR"
export MAVEN_OPTS

exec java ${MAVEN_OPTS} -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"

