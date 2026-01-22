#!/bin/bash
# Docker entrypoint for CRaC-enabled Ktor application
#
# Usage:
#   docker run <image>                    # Run normally
#   docker run <image> checkpoint         # Create checkpoint and exit
#   docker run <image> restore            # Restore from checkpoint

set -e

CHECKPOINT_DIR="/app/checkpoint"
JAR_FILE="/app/app.jar"

case "${1:-run}" in
    checkpoint)
        echo "Creating checkpoint..."
        java -XX:CRaCCheckpointTo="$CHECKPOINT_DIR" -jar "$JAR_FILE" --checkpoint
        echo "Checkpoint created at $CHECKPOINT_DIR"
        ;;
    restore)
        if [ ! -d "$CHECKPOINT_DIR" ] || [ -z "$(ls -A "$CHECKPOINT_DIR" 2>/dev/null)" ]; then
            echo "Error: No checkpoint found at $CHECKPOINT_DIR"
            echo "Run with 'checkpoint' argument first."
            exit 1
        fi
        echo "Restoring from checkpoint..."
        java -XX:CRaCRestoreFrom="$CHECKPOINT_DIR" -jar "$JAR_FILE"
        ;;
    run|*)
        echo "Starting application normally..."
        java -jar "$JAR_FILE"
        ;;
esac
