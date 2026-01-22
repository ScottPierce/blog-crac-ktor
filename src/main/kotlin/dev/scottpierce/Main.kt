package dev.scottpierce

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import org.crac.Core
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()
    val shouldCheckpoint = args.contains("--checkpoint")

    val server = embeddedServer(Netty, port = 8080) {
        configureRouting()
    }

    // Register CRaC resource for checkpoint/restore lifecycle
    ServerCracResource.register(server)

    server.start(wait = false)

    val elapsed = System.currentTimeMillis() - startTime
    logger.info("Server started in ${elapsed}ms on http://localhost:8080")
    logger.info("Endpoints: GET / and GET /health")

    if (shouldCheckpoint) {
        logger.info("--checkpoint flag detected, triggering checkpoint...")
        try {
            Core.checkpointRestore()
            // After restore, we continue from here
            logger.info("Restored from checkpoint!")
        } catch (e: Exception) {
            logger.error("Checkpoint failed: ${e.message}")
            throw e
        }
    }

    // Keep the application running
    runBlocking { awaitCancellation() }
}
