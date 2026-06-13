// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class TranscriptTextDoneEventLogprob(
    /**
     * The token that was used to generate the log probability.
     */
    val token: String? = null,
    /**
     * The log probability of the token.
     */
    val logprob: Double? = null,
    /**
     * The bytes that were used to generate the log probability.
     */
    val bytes: List<Int>? = null
)
