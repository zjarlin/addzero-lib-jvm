// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents token usage details including input tokens, output tokens, a breakdown of output tokens,
 * and the total tokens used. Only populated on batches created after September 7, 2025.
 */
@Serializable
data class BatchUsage(
    /**
     * The number of input tokens.
     */
    @SerialName("input_tokens")
    val inputTokens: Int,
    /**
     * A detailed breakdown of the input tokens.
     */
    @SerialName("input_tokens_details")
    val inputTokensDetails: site.addzero.api.openai.models.BatchUsageInputTokensDetails,
    /**
     * The number of output tokens.
     */
    @SerialName("output_tokens")
    val outputTokens: Int,
    /**
     * A detailed breakdown of the output tokens.
     */
    @SerialName("output_tokens_details")
    val outputTokensDetails: site.addzero.api.openai.models.BatchUsageOutputTokensDetails,
    /**
     * The total number of tokens used.
     */
    @SerialName("total_tokens")
    val totalTokens: Int
)
