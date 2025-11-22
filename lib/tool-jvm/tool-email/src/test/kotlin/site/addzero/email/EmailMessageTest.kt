package site.addzero.email

import org.junit.jupiter.api.Test
import site.addzero.email.model.EmailMessage
import kotlin.test.assertEquals

class EmailMessageTest {

    @Test
    fun `test email message creation with minimal fields`() {
        val message = EmailMessage(
            from = "sender@example.com",
            to = listOf("recipient@example.com"),
            subject = "Test Subject",
            textContent = "Test Content"
        )

        assertEquals("sender@example.com", message.from)
        assertEquals(listOf("recipient@example.com"), message.to)
        assertEquals(emptyList<String>(), message.cc)
        assertEquals(emptyList<String>(), message.bcc)
        assertEquals("Test Subject", message.subject)
        assertEquals("Test Content", message.textContent)
        assertEquals(null, message.htmlContent)
        assertEquals(emptyList<String>(), message.attachments)
    }

    @Test
    fun `test email message creation with all fields`() {
        val message = EmailMessage(
            from = "sender@example.com",
            to = listOf("recipient1@example.com", "recipient2@example.com"),
            cc = listOf("cc1@example.com", "cc2@example.com"),
            bcc = listOf("bcc1@example.com", "bcc2@example.com"),
            subject = "Test Subject",
            textContent = "Test Text Content",
            htmlContent = "<h1>Test HTML Content</h1>",
            attachments = listOf("/path/to/file1.txt", "/path/to/file2.txt")
        )

        assertEquals("sender@example.com", message.from)
        assertEquals(listOf("recipient1@example.com", "recipient2@example.com"), message.to)
        assertEquals(listOf("cc1@example.com", "cc2@example.com"), message.cc)
        assertEquals(listOf("bcc1@example.com", "bcc2@example.com"), message.bcc)
        assertEquals("Test Subject", message.subject)
        assertEquals("Test Text Content", message.textContent)
        assertEquals("<h1>Test HTML Content</h1>", message.htmlContent)
        assertEquals(listOf("/path/to/file1.txt", "/path/to/file2.txt"), message.attachments)
    }
}
