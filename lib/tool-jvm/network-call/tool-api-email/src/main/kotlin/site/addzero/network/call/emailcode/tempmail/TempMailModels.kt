package site.addzero.network.call.emailcode.tempmail

import site.addzero.network.call.emailcode.model.MailAddress
import java.time.Instant

data class TempMailDomain(
    val id: String,
    val domain: String,
    val isActive: Boolean,
    val isPrivate: Boolean,
)

data class TempMailMailbox(
    val address: String,
    val password: String,
    val accountId: String,
    val token: String,
)

data class TempMailMessageSummary(
    val id: String,
    val fromAddress: String,
    val fromName: String,
    val subject: String,
    val previewText: String,
    val seen: Boolean,
    val createdAt: Instant?,
)

data class TempMailMessageDetail(
    val id: String,
    val fromAddress: String,
    val fromName: String,
    val to: List<MailAddress>,
    val subject: String,
    val text: String,
    val html: String,
    val createdAt: Instant?,
)
