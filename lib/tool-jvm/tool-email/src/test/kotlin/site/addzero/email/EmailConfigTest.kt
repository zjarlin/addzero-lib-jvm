package site.addzero.email

import org.junit.jupiter.api.Test
import site.addzero.email.config.EmailConfig
import kotlin.test.assertEquals

class EmailConfigTest {

    @Test
    fun `test email config creation with default values`() {
        val config = EmailConfig(
            host = "smtp.example.com",
            port = 587,
            username = "user@example.com",
            password = "password"
        )

        assertEquals("smtp.example.com", config.host)
        assertEquals(587, config.port)
        assertEquals("user@example.com", config.username)
        assertEquals("password", config.password)
        assertEquals("smtp", config.protocol)
        assertEquals(false, config.enableSSL)
        assertEquals(true, config.enableTLS)
    }

    @Test
    fun `test email config creation with custom values`() {
        val config = EmailConfig(
            host = "smtp.example.com",
            port = 465,
            username = "user@example.com",
            password = "password",
            protocol = "smtps",
            enableSSL = true,
            enableTLS = false
        )

        assertEquals("smtp.example.com", config.host)
        assertEquals(465, config.port)
        assertEquals("user@example.com", config.username)
        assertEquals("password", config.password)
        assertEquals("smtps", config.protocol)
        assertEquals(true, config.enableSSL)
        assertEquals(false, config.enableTLS)
    }
}
