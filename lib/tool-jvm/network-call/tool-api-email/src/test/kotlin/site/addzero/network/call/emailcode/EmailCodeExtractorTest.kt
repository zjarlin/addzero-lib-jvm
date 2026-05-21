package site.addzero.network.call.emailcode

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import site.addzero.network.call.emailcode.model.EmailCodeRequest
import site.addzero.network.call.emailcode.model.EmailMessageDetail
import site.addzero.network.call.emailcode.model.EmailMessageSummary

class EmailCodeExtractorTest {
    @Test
    fun `extracts six-digit code from body`() {
        val summary = EmailMessageSummary(
            id = "msg-1",
            fromAddress = "no-reply@example.com",
            subject = "Your verification code",
        )
        val detail = EmailMessageDetail(
            id = "msg-1",
            fromAddress = "no-reply@example.com",
            subject = "Your verification code",
            text = "Use 123456 to finish sign in.",
        )

        val code = EmailCodeExtractor.extract(summary, detail, EmailCodeRequest())

        assertEquals("123456", code)
    }

    @Test
    fun `supports spaced digits`() {
        val summary = EmailMessageSummary(
            id = "msg-2",
            fromAddress = "no-reply@example.com",
            subject = "Code",
        )
        val detail = EmailMessageDetail(
            id = "msg-2",
            fromAddress = "no-reply@example.com",
            subject = "Code",
            html = "<p>1 2 3 4 5 6</p>",
        )

        val code = EmailCodeExtractor.extract(summary, detail, EmailCodeRequest())

        assertEquals("123456", code)
    }

    @Test
    fun `honors sender and subject filters`() {
        val summary = EmailMessageSummary(
            id = "msg-3",
            fromAddress = "alert@example.com",
            subject = "Password reset",
        )
        val detail = EmailMessageDetail(
            id = "msg-3",
            fromAddress = "alert@example.com",
            subject = "Password reset",
            text = "Your code is 654321",
        )

        val code = EmailCodeExtractor.extract(
            summary,
            detail,
            EmailCodeRequest(
                senderIncludes = listOf("no-reply"),
                subjectIncludes = listOf("verification"),
            ),
        )

        assertNull(code)
    }
}
