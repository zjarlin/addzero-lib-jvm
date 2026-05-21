package site.addzero.network.call.emailcode

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import site.addzero.network.call.emailcode.model.EmailCodeRequest
import site.addzero.network.call.emailcode.model.EmailMessageDetail
import site.addzero.network.call.emailcode.model.EmailMessageSummary
import java.time.Instant

class AbstractPollingEmailCodeMailboxTest {
    @Test
    fun `awaitCode ignores existing messages and returns new code`() {
        val mailbox = FakeMailbox()

        val result = mailbox.awaitCode(
            EmailCodeRequest(
                timeoutMs = 100,
                pollIntervalMs = 1,
                senderIncludes = listOf("sender@example.com"),
            ),
        )

        assertEquals("654321", result.code)
        assertEquals("msg-new", result.messageId)
    }

    private class FakeMailbox : AbstractPollingEmailCodeMailbox() {
        private var listCalls = 0

        override val providerId: String = "fake"
        override val address: String = "test@example.com"

        override fun listMessages(page: Int, pageSize: Int): List<EmailMessageSummary> {
            listCalls += 1
            return if (listCalls == 1) {
                listOf(
                    EmailMessageSummary(
                        id = "msg-old",
                        fromAddress = "sender@example.com",
                        subject = "Old code 111111",
                        createdAt = Instant.parse("2026-05-20T00:00:00Z"),
                    ),
                )
            } else {
                listOf(
                    EmailMessageSummary(
                        id = "msg-old",
                        fromAddress = "sender@example.com",
                        subject = "Old code 111111",
                        createdAt = Instant.parse("2026-05-20T00:00:00Z"),
                    ),
                    EmailMessageSummary(
                        id = "msg-new",
                        fromAddress = "sender@example.com",
                        subject = "New code",
                        createdAt = Instant.parse("2026-05-20T00:00:05Z"),
                    ),
                )
            }
        }

        override fun getMessage(messageId: String): EmailMessageDetail =
            when (messageId) {
                "msg-old" -> EmailMessageDetail(
                    id = "msg-old",
                    fromAddress = "sender@example.com",
                    subject = "Old code",
                    text = "111111",
                )

                else -> EmailMessageDetail(
                    id = "msg-new",
                    fromAddress = "sender@example.com",
                    subject = "New code",
                    text = "654321",
                )
            }
    }
}
