package dev.scottpierce

import io.ktor.server.engine.*
import org.crac.Context
import org.crac.Core
import org.crac.Resource
import org.slf4j.LoggerFactory

/**
 * CRaC Resource implementation for managing Ktor/Netty server lifecycle
 * during checkpoint and restore operations.
 *
 * Before checkpoint: Stops the server to release network resources
 * After restore: Restarts the server to resume handling requests
 *
 * Usage:
 * ```
 * val server = embeddedServer(Netty, port = 8080) { ... }
 * ServerCracResource.register(server)
 * server.start(wait = true)
 * ```
 */
class ServerCracResource private constructor(
    private val server: EmbeddedServer<*, *>
) : Resource {

    /**
     * Called before a checkpoint is created.
     * Stops the Netty server to release all network sockets and resources.
     */
    override fun beforeCheckpoint(context: Context<out Resource>?) {
        logger.info("CRaC: beforeCheckpoint - stopping server...")
        server.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
        logger.info("CRaC: Server stopped, ready for checkpoint")
    }

    /**
     * Called after restoring from a checkpoint.
     * Restarts the Netty server to resume accepting connections.
     */
    override fun afterRestore(context: Context<out Resource>?) {
        val restoreStart = System.currentTimeMillis()
        logger.info("CRaC: afterRestore - restarting server...")
        server.start(wait = false)
        val elapsed = System.currentTimeMillis() - restoreStart
        logger.info("CRaC: Server restarted in ${elapsed}ms")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ServerCracResource::class.java)

        /**
         * Register a server with the CRaC global context for checkpoint/restore lifecycle management.
         * Must be called during application startup, before starting the server.
         */
        fun register(server: EmbeddedServer<*, *>) {
            val resource = ServerCracResource(server)
            Core.getGlobalContext().register(resource)
            logger.info("CRaC resource registered")
        }
    }
}
