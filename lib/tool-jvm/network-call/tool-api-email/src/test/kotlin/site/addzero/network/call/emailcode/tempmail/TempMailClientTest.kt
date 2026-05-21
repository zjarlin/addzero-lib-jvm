package site.addzero.network.call.emailcode.tempmail

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TempMailClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: TempMailClient

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = TempMailClient(baseUrl = server.url("/").toString())
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `creates mailbox using preferred domain`() {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "hydra:member": [
                    { "id": "domain-1", "domain": "a.example", "isActive": true, "isPrivate": false },
                    { "id": "domain-2", "domain": "b.example", "isActive": true, "isPrivate": false }
                  ]
                }
                """.trimIndent(),
            ),
        )
        server.enqueue(MockResponse().setBody("""{ "id": "account-1" }"""))
        server.enqueue(MockResponse().setBody("""{ "token": "token-1" }"""))

        val mailbox = client.createMailboxAndLogin(
            TempMailCreateMailboxRequest(
                prefix = "demo",
                preferredDomain = "b.example",
            ),
        )

        assertTrue(mailbox.address.endsWith("@b.example"))
        assertEquals("account-1", mailbox.accountId)
        assertEquals("token-1", mailbox.token)
        assertTrue(mailbox.password.length >= 8)
    }

    @Test
    fun `maps list and detail payloads`() {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "hydra:member": [
                    {
                      "id": "msg-1",
                      "from": { "address": "no-reply@example.com", "name": "Example" },
                      "subject": "Your code",
                      "intro": "Use 123456",
                      "seen": false,
                      "createdAt": "2026-05-20T10:00:00Z"
                    }
                  ]
                }
                """.trimIndent(),
            ),
        )
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "id": "msg-1",
                  "from": { "address": "no-reply@example.com", "name": "Example" },
                  "to": [{ "address": "user@example.com", "name": "User" }],
                  "subject": "Your code",
                  "text": "Use 123456",
                  "html": ["<p>Use <b>123456</b></p>"],
                  "createdAt": "2026-05-20T10:00:00Z"
                }
                """.trimIndent(),
            ),
        )

        val summaries = client.listMessages("token-1")
        val detail = client.getMessage("token-1", "msg-1")

        assertEquals(1, summaries.size)
        assertEquals("msg-1", summaries.first().id)
        assertFalse(summaries.first().seen)
        assertEquals("msg-1", detail.id)
        assertEquals("user@example.com", detail.to.first().address)
        assertTrue(detail.html.contains("123456"))
    }
}
