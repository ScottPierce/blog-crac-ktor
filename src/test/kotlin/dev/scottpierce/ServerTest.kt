package dev.scottpierce

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Basic server endpoint tests using Ktor test host.
 */
class ServerTest {

    @Test
    fun `root endpoint returns Hello CRaC`() = testApplication {
        application {
            configureRouting()
        }

        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, CRaC!", response.bodyAsText())
    }

    @Test
    fun `health endpoint returns OK`() = testApplication {
        application {
            configureRouting()
        }

        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }
}
