package site.addzero.email

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import site.addzero.email.config.EmailConfig
import site.addzero.email.model.EmailMessage

class SmtpEmailSenderTest {

    private lateinit var config: EmailConfig

    @BeforeEach
    fun setUp() {
        // QQ邮箱配置
        config = EmailConfig(
            host = "smtp.qq.com",
            port = 587,
            username = "825755501@qq.com",
            password = "qcmcboahxsgtbdbe",
            protocol = "smtp",
            enableSSL = false,
            enableTLS = true
        )
    }

    /**
     * 测试SMTP邮件发送器的创建
     */
    @Test
    fun `test smtp email sender creation`() {
        val sender = EmailSender.createSmtpSender(config)
        assert(sender is SmtpEmailSender)
    }

    /**
     * 测试真实邮件发送功能
     * 仅在明确需要发送真实邮件时启用此测试
     * 通过设置环境变量 ENABLE_REAL_EMAIL_TEST=true 来启用
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_REAL_EMAIL_TEST", matches = "true")
    fun `test real email sending via smtp sender`() {
        val sender = EmailSender.createSmtpSender(config)

        val message = EmailMessage(
            from = "825755501@qq.com",
            to = listOf("zjarlin@outlook.com"),
            subject = "SMTP Real Test Email",
            textContent = "This is a real test email sent via SMTP sender.",
            htmlContent = "<h1>SMTP Real Test Email</h1><p>This is a real test email sent via SMTP sender.</p>"
        )

        sender.send(message)
        println("Email sent successfully.")
    }
}
