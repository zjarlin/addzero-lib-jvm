package site.addzero.network.call.emailcode.model

data class EmailCodeRequest(
    val timeoutMs: Long = 60_000,
    val pollIntervalMs: Long = 3_000,
    val codeLength: Int = 6,
    val codePattern: Regex? = null,
    val senderIncludes: List<String> = emptyList(),
    val subjectIncludes: List<String> = emptyList(),
    val bodyIncludes: List<String> = emptyList(),
    val ignoreExistingMessages: Boolean = true,
    val baselineMessageIds: Set<String> = emptySet(),
    val maxMessagesPerPoll: Int = 20,
)
