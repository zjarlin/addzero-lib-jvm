package site.addzero.network.call.tempmail

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
    val intro: String,
    val seen: Boolean,
    val createdAt: String,
)

data class TempMailRecipient(
    val address: String,
    val name: String,
)

data class TempMailMessageDetail(
    val id: String,
    val fromAddress: String,
    val fromName: String,
    val to: List<TempMailRecipient>,
    val subject: String,
    val text: String,
    val html: String,
    val createdAt: String,
)
