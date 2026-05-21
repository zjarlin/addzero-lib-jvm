package site.addzero.network.call.emailcode.model

import java.time.Instant

data class EmailCodeResult(
    val code: String,
    val messageId: String,
    val providerId: String,
    val address: String,
    val fromAddress: String,
    val subject: String,
    val receivedAt: Instant? = null,
)
