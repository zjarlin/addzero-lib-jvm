// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A logprob is the logarithmic probability that the model assigns to producing a particular token at a
 * given position in the sequence. Less-negative (higher) logprob values indicate greater model
 * confidence in that token choice.
 */
@Serializable
data class ResponseLogProb(
    /**
     * A possible text token.
     */
    val token: String,
    /**
     * The log probability of this token.
     */
    val logprob: Double,
    /**
     * The log probabilities of up to 20 of the most likely tokens.
     */
    @SerialName("top_logprobs")
    val topLogprobs: List<site.addzero.api.openai.models.ResponseLogProbTopLogprob>? = null
)
