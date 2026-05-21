package site.addzero.network.call.emailcode.tempmail

import site.addzero.network.call.emailcode.AbstractPollingEmailCodeMailbox
import site.addzero.network.call.emailcode.model.EmailMessageDetail
import site.addzero.network.call.emailcode.model.EmailMessageSummary

class TempMailEmailCodeMailbox(
    private val client: TempMailClient,
    private val mailbox: TempMailMailbox,
) : AbstractPollingEmailCodeMailbox() {
    override val providerId: String = TempMailEmailCodeProvider.ID
    override val address: String = mailbox.address
    override val loginSecret: String = mailbox.password

    override fun listMessages(page: Int, pageSize: Int): List<EmailMessageSummary> =
        client.listMessages(mailbox.token, page)
            .take(pageSize)
            .map { message ->
                EmailMessageSummary(
                    id = message.id,
                    fromAddress = message.fromAddress,
                    fromName = message.fromName,
                    subject = message.subject,
                    previewText = message.previewText,
                    seen = message.seen,
                    createdAt = message.createdAt,
                )
            }

    override fun getMessage(messageId: String): EmailMessageDetail {
        val message = client.getMessage(mailbox.token, messageId)
        return EmailMessageDetail(
            id = message.id,
            fromAddress = message.fromAddress,
            fromName = message.fromName,
            to = message.to,
            subject = message.subject,
            text = message.text,
            html = message.html,
            createdAt = message.createdAt,
        )
    }
}
