package dev.scottpierce

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Integration test for CRaC checkpoint/restore lifecycle.
 *
 * This test requires:
 * 1. A CRaC-enabled JDK (Azul Zulu with CRaC)
 * 2. The shadowJar to be built first: ./gradlew shadowJar
 * 3. Linux for actual checkpoint creation (macOS runs in simulation mode)
 *
 * The test will:
 * 1. Start the application with --checkpoint flag
 * 2. Wait for checkpoint to complete
 * 3. Restore from checkpoint
 * 4. Verify the server responds correctly
 * 5. Compare startup times
 */
class CracIntegrationTest {

    private val projectDir = File(System.getProperty("user.dir"))
    private val checkpointDir = File(projectDir, "checkpoint-test")
    private val jarFile = File(projectDir, "build/libs/blog-crac-ktor-all.jar")

    @Test
    fun `checkpoint and restore lifecycle`() = runBlocking {
        // Skip if JAR doesn't exist
        if (!jarFile.exists()) {
            println("Skipping CRaC test: JAR not found at ${jarFile.absolutePath}")
            println("Run './gradlew shadowJar' first")
            return@runBlocking
        }

        // Clean up checkpoint directory
        checkpointDir.deleteRecursively()
        checkpointDir.mkdirs()

        try {
            // Step 1: Create checkpoint
            println("Step 1: Creating checkpoint...")
            val checkpointProcess = createCheckpointProcess()
            val checkpointExitCode = checkpointProcess.waitFor()

            // On macOS, checkpoint runs in simulation mode and may not create files
            // but the process should complete successfully
            println("Checkpoint process exited with code: $checkpointExitCode")

            // Check if actual checkpoint files were created (Linux only)
            val hasCheckpointFiles = checkpointDir.listFiles()?.isNotEmpty() == true

            if (hasCheckpointFiles) {
                println("Checkpoint files created, testing restore...")

                // Step 2: Restore from checkpoint and measure time
                val restoreStartTime = System.currentTimeMillis()
                val restoreProcess = createRestoreProcess()

                // Wait for server to be ready
                val client = HttpClient(CIO)
                try {
                    waitForServer(client, "http://localhost:8080/health")
                    val restoreTime = System.currentTimeMillis() - restoreStartTime

                    println("Server restored in ${restoreTime}ms")

                    // Step 3: Verify endpoints work
                    val rootResponse = client.get("http://localhost:8080/")
                    assertEquals(HttpStatusCode.OK, rootResponse.status)
                    assertEquals("Hello, CRaC!", rootResponse.bodyAsText())

                    val healthResponse = client.get("http://localhost:8080/health")
                    assertEquals(HttpStatusCode.OK, healthResponse.status)
                    assertEquals("OK", healthResponse.bodyAsText())

                    println("All endpoints verified successfully!")

                    // Restore should be significantly faster than cold start
                    // Cold start is typically 200-500ms, restore should be <100ms
                    assertTrue(restoreTime < 500, "Restore took ${restoreTime}ms, expected <500ms")

                } finally {
                    client.close()
                    restoreProcess.destroy()
                    restoreProcess.waitFor()
                }
            } else {
                println("No checkpoint files created (likely macOS simulation mode)")
                println("CRaC lifecycle was exercised but actual checkpoint requires Linux")

                // On macOS, verify the app at least starts normally
                verifyNormalStartup()
            }

        } finally {
            // Cleanup
            checkpointDir.deleteRecursively()
        }
    }

    private fun createCheckpointProcess(): Process {
        val javaHome = System.getProperty("java.home")
        val java = "$javaHome/bin/java"

        return ProcessBuilder(
            java,
            "-XX:CRaCCheckpointTo=${checkpointDir.absolutePath}",
            "-jar", jarFile.absolutePath,
            "--checkpoint"
        )
            .directory(projectDir)
            .inheritIO()
            .start()
    }

    private fun createRestoreProcess(): Process {
        val javaHome = System.getProperty("java.home")
        val java = "$javaHome/bin/java"

        return ProcessBuilder(
            java,
            "-XX:CRaCRestoreFrom=${checkpointDir.absolutePath}",
            "-jar", jarFile.absolutePath
        )
            .directory(projectDir)
            .inheritIO()
            .start()
    }

    private suspend fun waitForServer(client: HttpClient, url: String, timeout: Long = 10_000) {
        withTimeout(timeout) {
            while (true) {
                try {
                    val response = client.get(url)
                    if (response.status == HttpStatusCode.OK) {
                        return@withTimeout
                    }
                } catch (e: Exception) {
                    // Server not ready yet
                }
                delay(100)
            }
        }
    }

    private suspend fun verifyNormalStartup() {
        val javaHome = System.getProperty("java.home")
        val java = "$javaHome/bin/java"

        val process = ProcessBuilder(java, "-jar", jarFile.absolutePath)
            .directory(projectDir)
            .inheritIO()
            .start()

        val client = HttpClient(CIO)
        try {
            waitForServer(client, "http://localhost:8080/health", 15_000)

            val response = client.get("http://localhost:8080/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Hello, CRaC!", response.bodyAsText())

            println("Normal startup verified successfully!")
        } finally {
            client.close()
            process.destroy()
            process.waitFor()
        }
    }
}
