package site.addzero.email

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.assertDoesNotThrow
import site.addzero.email.config.EmailConfig
import site.addzero.email.model.EmailMessage

class EmailToolTest {
    
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
    
    @Test
    fun `test email tool creation`() {
        assertDoesNotThrow {
            val message = EmailMessage(
                from = "825755501@qq.com",
                to = listOf("recipient@example.com"),
                subject = "Test Email",
                textContent = "This is a test email."
            )
            
            // 这里只是测试对象创建，不会实际发送邮件
            // 在实际使用中，需要配置真实的邮件服务器
        }
    }
    
    /**
     * 仅在明确需要发送真实邮件时启用此测试
     * 通过设置环境变量 ENABLE_REAL_EMAIL_TEST=true 来启用
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_REAL_EMAIL_TEST", matches = "true")
    fun `test real email sending`() {
        val message = EmailMessage(
            from = "825755501@qq.com",
            to = listOf("recipient@example.com"),
            subject = "Real Test Email",
            textContent = "This is a real test email sent from the unit test.",
            htmlContent = "<h1>This is a real test email</h1><p>Sent from the unit test.</p>"
        )
        
        send(config, message)
    }
}