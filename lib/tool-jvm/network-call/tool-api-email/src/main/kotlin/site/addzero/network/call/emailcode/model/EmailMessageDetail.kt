package site.addzero.network.call.emailcode.model

import java.time.Instant

data class EmailMessageDetail(
    val id: String,
    val fromAddress: String = "",
    val fromName: String = "",
    val to: List<MailAddress> = emptyList(),
    val subject: String = "",
    val text: String = "",
    val html: String = "",
    val createdAt: Instant? = null,
) {
    companion object {
        fun fromSummary(summary: EmailMessageSummary): EmailMessageDetail =
            EmailMessageDetail(
                id = summary.id,
                fromAddress = summary.fromAddress,
                fromName = summary.fromName,
                subject = summary.subject,
                text = summary.previewText,
                createdAt = summary.createdAt,
            )
    }
}
