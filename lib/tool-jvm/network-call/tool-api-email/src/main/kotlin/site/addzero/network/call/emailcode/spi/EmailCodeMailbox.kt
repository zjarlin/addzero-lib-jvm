package site.addzero.network.call.emailcode.spi

import site.addzero.network.call.emailcode.model.EmailCodeRequest
import site.addzero.network.call.emailcode.model.EmailCodeResult
import site.addzero.network.call.emailcode.model.EmailMessageDetail
import site.addzero.network.call.emailcode.model.EmailMessageSummary

interface EmailCodeMailbox : AutoCloseable {
    val providerId: String
    val address: String
    val loginSecret: String?
        get() = null

    fun listMessages(page: Int = 1, pageSize: Int = 50): List<EmailMessageSummary>

    fun getMessage(messageId: String): EmailMessageDetail

    fun awaitCode(request: EmailCodeRequest = EmailCodeRequest()): EmailCodeResult

    override fun close() = Unit
}
