#!/bin/bash
# Build MacMicFix using Docker (works on any platform including macOS Intel)

set -e

echo "Building MacMicFix using Docker..."
echo "This works around the macOS Intel build issue."

docker run --rm \
  -v "$(pwd)":/workspace \
  -w /workspace \
  eclipse-temurin:21-jdk \
  bash -c "./gradlew build --no-daemon"

echo ""
echo "Build complete! JAR file is in build/libs/"
ls -lh build/libs/*.jar

