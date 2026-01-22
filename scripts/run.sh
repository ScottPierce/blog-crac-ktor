#!/bin/bash
# Run the application normally (without CRaC)
# Use this to measure baseline startup time

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo "Building application..."
./gradlew shadowJar --quiet

echo ""
echo "Starting application (normal mode, no CRaC)..."
echo "Press Ctrl+C to stop"
echo ""

java -jar build/libs/blog-crac-ktor-all.jar
