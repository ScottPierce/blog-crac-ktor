#!/bin/bash
# Restore from a CRaC checkpoint
# This demonstrates the fast startup time from a checkpoint

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
CHECKPOINT_DIR="$PROJECT_DIR/checkpoint"

cd "$PROJECT_DIR"

if [ ! -d "$CHECKPOINT_DIR" ] || [ -z "$(ls -A "$CHECKPOINT_DIR" 2>/dev/null)" ]; then
    echo "Error: No checkpoint found at $CHECKPOINT_DIR"
    echo "Run ./scripts/checkpoint.sh first to create a checkpoint."
    echo ""
    echo "Note: On macOS, CRaC runs in simulation mode and doesn't create"
    echo "actual checkpoint files. Use Docker for full CRaC functionality."
    exit 1
fi

echo "Restoring from checkpoint: $CHECKPOINT_DIR"
echo "Press Ctrl+C to stop"
echo ""

# Restore from checkpoint - this should be nearly instant
java -XX:CRaCRestoreFrom="$CHECKPOINT_DIR" -jar build/libs/blog-crac-ktor-all.jar
