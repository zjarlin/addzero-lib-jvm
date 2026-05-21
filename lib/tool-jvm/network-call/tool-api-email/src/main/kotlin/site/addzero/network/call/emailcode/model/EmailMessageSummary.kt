package site.addzero.network.call.emailcode.model

import java.time.Instant

data class EmailMessageSummary(
    val id: String,
    val fromAddress: String = "",
    val fromName: String = "",
    val subject: String = "",
    val previewText: String = "",
    val seen: Boolean? = null,
    val createdAt: Instant? = null,
)
