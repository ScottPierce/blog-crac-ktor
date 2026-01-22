#!/bin/bash
# Create a CRaC checkpoint using the --checkpoint flag
# The application will start, become ready, and then trigger its own checkpoint

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
CHECKPOINT_DIR="$PROJECT_DIR/checkpoint"

cd "$PROJECT_DIR"

echo "Building application..."
./gradlew shadowJar --quiet

# Clean up previous checkpoint
rm -rf "$CHECKPOINT_DIR"
mkdir -p "$CHECKPOINT_DIR"

echo ""
echo "Starting application with --checkpoint flag..."
echo "Checkpoint will be saved to: $CHECKPOINT_DIR"
echo ""

# Run with CRaC checkpoint support and --checkpoint flag
# The app will start, become ready, then trigger checkpoint and exit
java -XX:CRaCCheckpointTo="$CHECKPOINT_DIR" -jar build/libs/blog-crac-ktor-all.jar --checkpoint

echo ""
if [ -d "$CHECKPOINT_DIR" ] && [ "$(ls -A "$CHECKPOINT_DIR" 2>/dev/null)" ]; then
    echo "Checkpoint created successfully!"
    echo "Checkpoint location: $CHECKPOINT_DIR"
    ls -la "$CHECKPOINT_DIR"
    echo ""
    echo "To restore, run: ./scripts/restore.sh"
else
    echo "Checkpoint may have been simulated (macOS) or failed."
    echo "For full CRaC functionality, use Docker on Linux."
fi
