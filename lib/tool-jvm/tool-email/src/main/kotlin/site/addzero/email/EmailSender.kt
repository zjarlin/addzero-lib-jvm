package site.addzero.email

import site.addzero.email.config.EmailConfig
import site.addzero.email.model.EmailMessage
import site.addzero.email.exception.EmailException

/**
 * 邮件发送器接口
 */
interface EmailSender {
    
    /**
     * 发送邮件
     *
     * @param message 邮件消息对象
     * @throws EmailException 邮件发送失败时抛出
     */
    @Throws(EmailException::class)
    fun send(message: EmailMessage)
    
    companion object {
        /**
         * 创建SMTP邮件发送器实例
         *
         * @param config 邮件服务器配置
         * @return EmailSender 实例
         */
        fun createSmtpSender(config: EmailConfig): EmailSender {
            return SmtpEmailSender(config)
        }
    }
}