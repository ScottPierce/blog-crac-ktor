# CRaC + Ktor Example

A simple example demonstrating [Coordinated Restore at Checkpoint (CRaC)](https://openjdk.org/projects/crac/) with a Ktor server using the Netty engine.

## Blog Post

For a detailed explanation of CRaC, how it works, and why you might want to use it, read the accompanying blog post:

**[Fast JVM Startup with CRaC and Ktor](https://scottpierce.dev/posts/ktor-and-crac/)**

## Quick Start

```bash
# Build
./gradlew build

# Run normally
./scripts/run.sh

# Create checkpoint (requires Linux)
./scripts/checkpoint.sh

# Restore from checkpoint
./scripts/restore.sh