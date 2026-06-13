// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The log probability of a token.
 */
@Serializable
data class LogProb(
    val token: String,
    val logprob: Double,
    val bytes: List<Int>,
    @SerialName("top_logprobs")
    val topLogprobs: List<site.addzero.api.openai.models.TopLogProb>
)
