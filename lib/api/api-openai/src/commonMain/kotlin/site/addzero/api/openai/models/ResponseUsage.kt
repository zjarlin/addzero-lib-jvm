// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents token usage details including input tokens, output tokens, a breakdown of output tokens,
 * and the total tokens used.
 */
@Serializable
data class ResponseUsage(
    /**
     * The number of input tokens.
     */
    @SerialName("input_tokens")
    val inputTokens: Int,
    /**
     * A detailed breakdown of the input tokens.
     */
    @SerialName("input_tokens_details")
    val inputTokensDetails: site.addzero.api.openai.models.ResponseUsageInputTokensDetails,
    /**
     * The number of output tokens.
     */
    @SerialName("output_tokens")
    val outputTokens: Int,
    /**
     * A detailed breakdown of the output tokens.
     */
    @SerialName("output_tokens_details")
    val outputTokensDetails: site.addzero.api.openai.models.ResponseUsageOutputTokensDetails,
    /**
     * The total number of tokens used.
     */
    @SerialName("total_tokens")
    val totalTokens: Int
)
