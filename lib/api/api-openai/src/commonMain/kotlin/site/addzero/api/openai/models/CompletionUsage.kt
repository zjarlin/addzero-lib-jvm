// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Usage statistics for the completion request.
 */
@Serializable
data class CompletionUsage(
    /**
     * Number of tokens in the generated completion.
     */
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    /**
     * Number of tokens in the prompt.
     */
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    /**
     * Total number of tokens used in the request (prompt + completion).
     */
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
    /**
     * Breakdown of tokens used in a completion.
     */
    @SerialName("completion_tokens_details")
    val completionTokensDetails: site.addzero.api.openai.models.CompletionUsageCompletionTokensDetails? = null,
    /**
     * Breakdown of tokens used in the prompt.
     */
    @SerialName("prompt_tokens_details")
    val promptTokensDetails: site.addzero.api.openai.models.CompletionUsagePromptTokensDetails? = null
)
