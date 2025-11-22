package site.addzero.email

import jakarta.mail.*
import jakarta.mail.internet.*
import site.addzero.email.config.EmailConfig
import site.addzero.email.exception.EmailException
import site.addzero.email.model.EmailMessage
import java.io.File
import java.util.*

/**
 * SMTP邮件发送器实现类
 */
internal class SmtpEmailSender(private val config: EmailConfig) : EmailSender {
    
    private val session: Session by lazy {
        val props = Properties().apply {
            put("mail.transport.protocol", config.protocol)
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            
            if (config.enableSSL) {
                put("mail.smtp.ssl.enable", "true")
            }
            
            if (config.enableTLS) {
                put("mail.smtp.starttls.enable", "true")
            }
            
            put("mail.smtp.auth", "true")
        }
        
        Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.username, config.password)
            }
        })
    }
    
    /**
     * 发送邮件
     *
     * @param message 邮件消息对象
     * @throws EmailException 邮件发送失败时抛出
     */
    @Throws(EmailException::class)
    override fun send(message: EmailMessage) {
        try {
            val mimeMessage = createMimeMessage(message)
            Transport.send(mimeMessage)
        } catch (e: MessagingException) {
            throw EmailException("Failed to send email: ${e.message}", e)
        }
    }
    
    /**
     * 创建MIME消息对象
     *
     * @param message 邮件消息对象
     * @return MimeMessage MIME消息对象
     * @throws MessagingException 消息创建失败时抛出
     */
    @Throws(MessagingException::class)
    private fun createMimeMessage(message: EmailMessage): MimeMessage {
        val mimeMessage = MimeMessage(session)
        
        // 设置发件人
        mimeMessage.setFrom(InternetAddress(message.from))
        
        // 设置收件人
        mimeMessage.setRecipients(
            Message.RecipientType.TO,
            message.to.map { InternetAddress(it) }.toTypedArray()
        )
        
        // 设置抄送
        if (message.cc.isNotEmpty()) {
            mimeMessage.setRecipients(
                Message.RecipientType.CC,
                message.cc.map { InternetAddress(it) }.toTypedArray()
            )
        }
        
        // 设置密送
        if (message.bcc.isNotEmpty()) {
            mimeMessage.setRecipients(
                Message.RecipientType.BCC,
                message.bcc.map { InternetAddress(it) }.toTypedArray()
            )
        }
        
        // 设置主题
        mimeMessage.subject = message.subject
        
        // 设置内容
        when {
            message.htmlContent != null && message.textContent != null -> {
                // 同时包含HTML和纯文本内容，创建多部分消息
                val multipart = MimeMultipart("alternative")
                
                val textPart = MimeBodyPart().apply {
                    setText(message.textContent, "utf-8")
                }
                multipart.addBodyPart(textPart)
                
                val htmlPart = MimeBodyPart().apply {
                    setContent(message.htmlContent, "text/html;charset=utf-8")
                }
                multipart.addBodyPart(htmlPart)
                
                mimeMessage.setContent(multipart)
            }
            message.htmlContent != null -> {
                // 只有HTML内容
                mimeMessage.setContent(message.htmlContent, "text/html;charset=utf-8")
            }
            message.textContent != null -> {
                // 只有纯文本内容
                mimeMessage.setText(message.textContent, "utf-8")
            }
            else -> {
                // 没有内容
                mimeMessage.setText("", "utf-8")
            }
        }
        
        // 添加附件
        if (message.attachments.isNotEmpty()) {
            val multipart = if (mimeMessage.content is MimeMultipart) {
                mimeMessage.content as MimeMultipart
            } else {
                val newMultipart = MimeMultipart()
                // 将现有内容添加为第一个部分
                val contentPart = MimeBodyPart().apply {
                    when {
                        message.htmlContent != null -> setContent(message.htmlContent, "text/html;charset=utf-8")
                        message.textContent != null -> setText(message.textContent, "utf-8")
                        else -> setText("", "utf-8")
                    }
                }
                newMultipart.addBodyPart(contentPart)
                newMultipart
            }
            
            // 添加附件
            message.attachments.forEach { filePath ->
                val attachmentPart = MimeBodyPart().apply {
                    attachFile(File(filePath))
                }
                multipart.addBodyPart(attachmentPart)
            }
            
            mimeMessage.setContent(multipart)
        }
        
        return mimeMessage
    }
}