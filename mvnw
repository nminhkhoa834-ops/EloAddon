set -e

# Determine project base dir
if [ -z "$MAVEN_PROJECTBASEDIR" ]; then
  MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
  export MAVEN_PROJECTBASEDIR
fi

# Read wrapper properties
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
if [ ! -f "$WRAPPER_PROPERTIES" ]; then
  echo "Error: $WRAPPER_PROPERTIES not found." >&2
  exit 1
fi

# Parse distributionUrl
DIST_URL=$(grep 'distributionUrl=' "$WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')
if [ -z "$DIST_URL" ]; then
  echo "Error: distributionUrl not found in $WRAPPER_PROPERTIES" >&2
  exit 1
fi

# Maven user home
MAVEN_USER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}"
WRAPPER_DIR="$MAVEN_USER_HOME/wrapper/dists"

# Extract Maven version from URL
DIST_NAME=$(basename "$DIST_URL" .zip)
MAVEN_HOME="$WRAPPER_DIR/$DIST_NAME"

# Download and extract if needed
if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Maven from: $DIST_URL"
  mkdir -p "$WRAPPER_DIR"
  TEMP_FILE="$WRAPPER_DIR/$DIST_NAME.zip"

  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$TEMP_FILE" "$DIST_URL"
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$TEMP_FILE" "$DIST_URL"
  else
    echo "Error: Neither curl nor wget found. Cannot download Maven." >&2
    exit 1
  fi

  echo "Extracting Maven..."
  unzip -q -d "$WRAPPER_DIR" "$TEMP_FILE"
  rm -f "$TEMP_FILE"
  chmod +x "$MAVEN_HOME/bin/mvn"
  echo "Maven installed to: $MAVEN_HOME"
fi

# Execute Maven
exec "$MAVEN_HOME/bin/mvn" "$@"

