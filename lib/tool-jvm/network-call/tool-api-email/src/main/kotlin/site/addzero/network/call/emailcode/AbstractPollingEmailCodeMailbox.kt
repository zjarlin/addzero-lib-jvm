package site.addzero.network.call.emailcode

import site.addzero.network.call.emailcode.model.EmailCodeRequest
import site.addzero.network.call.emailcode.model.EmailCodeResult
import site.addzero.network.call.emailcode.model.EmailMessageDetail
import site.addzero.network.call.emailcode.model.EmailMessageSummary
import site.addzero.network.call.emailcode.spi.EmailCodeMailbox
import java.time.Instant

abstract class AbstractPollingEmailCodeMailbox : EmailCodeMailbox {
    final override fun awaitCode(request: EmailCodeRequest): EmailCodeResult {
        require(request.timeoutMs > 0) { "timeoutMs must be > 0" }
        require(request.pollIntervalMs > 0) { "pollIntervalMs must be > 0" }
        require(request.codeLength > 0) { "codeLength must be > 0" }
        require(request.maxMessagesPerPoll > 0) { "maxMessagesPerPoll must be > 0" }

        val seenMessageIds = when {
            request.baselineMessageIds.isNotEmpty() -> request.baselineMessageIds.toMutableSet()
            request.ignoreExistingMessages -> listMessages(page = 1, pageSize = request.maxMessagesPerPoll)
                .mapTo(linkedSetOf()) { it.id }
            else -> linkedSetOf()
        }

        val deadline = System.currentTimeMillis() + request.timeoutMs
        while (true) {
            val messages = listMessages(page = 1, pageSize = request.maxMessagesPerPoll)
                .filterNot { it.id in seenMessageIds }
                .sortedWith(
                    compareByDescending<EmailMessageSummary> { it.createdAt ?: Instant.EPOCH }
                        .thenByDescending { it.id },
                )

            for (summary in messages) {
                val detail = runCatching { getMessage(summary.id) }.getOrElse { EmailMessageDetail.fromSummary(summary) }
                val code = EmailCodeExtractor.extract(summary, detail, request)
                seenMessageIds += summary.id
                if (code != null) {
                    return EmailCodeResult(
                        code = code,
                        messageId = summary.id,
                        providerId = providerId,
                        address = address,
                        fromAddress = detail.fromAddress.ifBlank { summary.fromAddress },
                        subject = detail.subject.ifBlank { summary.subject },
                        receivedAt = detail.createdAt ?: summary.createdAt,
                    )
                }
            }

            val remainingMs = deadline - System.currentTimeMillis()
            if (remainingMs <= 0) {
                break
            }
            Thread.sleep(minOf(request.pollIntervalMs, remainingMs))
        }

        error("Verification code not received within ${request.timeoutMs / 1000}s for $address via $providerId")
    }
}
