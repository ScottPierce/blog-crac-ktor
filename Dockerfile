# Multi-stage build for CRaC-enabled Ktor application

# Stage 1: Build the application
FROM gradle:8.12-jdk21 AS builder

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src

RUN gradle shadowJar --no-daemon

# Stage 2: Runtime with CRaC support
FROM azul/zulu-openjdk:21-jdk-crac-latest

# Install utilities for checkpoint management
RUN apt-get update && apt-get install -y \
    curl \
    procps \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the fat JAR from builder
COPY --from=builder /app/build/libs/blog-crac-ktor-all.jar /app/app.jar

# Copy entrypoint script
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# Create checkpoint directory
RUN mkdir -p /app/checkpoint

# Expose the application port
EXPOSE 8080

ENTRYPOINT ["/app/docker-entrypoint.sh"]
