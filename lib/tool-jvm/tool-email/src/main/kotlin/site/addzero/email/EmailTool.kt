@file:JvmName("EmailTool")

package site.addzero.email

import site.addzero.email.config.EmailConfig
import site.addzero.email.exception.EmailException
import site.addzero.email.model.EmailMessage

/**
 * 统一的邮件发送工具类
 *
 * 提供简化的邮件发送接口，隐藏底层实现细节
 */

private var defaultSender: EmailSender? = null

/**
 * 设置默认的邮件发送器
 *
 * @param sender 邮件发送器实例
 */
fun setDefaultSender(sender: EmailSender) {
    defaultSender = sender
}

/**
 * 使用默认配置发送邮件
 *
 * @param message 邮件消息对象
 * @throws EmailException 邮件发送失败时抛出
 * @throws IllegalStateException 当未设置默认发送器时抛出
 */
@Throws(EmailException::class, IllegalStateException::class)
fun send(message: EmailMessage) {
    val sender = defaultSender ?: throw IllegalStateException("Default email sender not configured")
    sender.send(message)
}

/**
 * 使用指定配置发送邮件
 *
 * @param config 邮件服务器配置
 * @param message 邮件消息对象
 * @throws EmailException 邮件发送失败时抛出
 */
@Throws(EmailException::class)
fun send(config: EmailConfig, message: EmailMessage) {
    val sender = EmailSender.createSmtpSender(config)
    sender.send(message)
}

/**
 * 快速发送简单文本邮件
 *
 * @param config 邮件服务器配置
 * @param from 发件人邮箱地址
 * @param to 收件人邮箱地址
 * @param subject 邮件主题
 * @param content 邮件内容
 * @throws EmailException 邮件发送失败时抛出
 */
@Throws(EmailException::class)
fun sendText(
    config: EmailConfig,
    from: String,
    to: String,
    subject: String,
    content: String
) {
    val message = EmailMessage(
        from = from,
        to = listOf(to),
        subject = subject,
        textContent = content
    )
    send(config, message)
}

/**
 * 快速发送HTML邮件
 *
 * @param config 邮件服务器配置
 * @param from 发件人邮箱地址
 * @param to 收件人邮箱地址
 * @param subject 邮件主题
 * @param htmlContent HTML邮件内容
 * @throws EmailException 邮件发送失败时抛出
 */
@Throws(EmailException::class)
fun sendHtml(
    config: EmailConfig,
    from: String,
    to: String,
    subject: String,
    htmlContent: String
) {
    val message = EmailMessage(
        from = from,
        to = listOf(to),
        subject = subject,
        htmlContent = htmlContent
    )
    send(config, message)
}