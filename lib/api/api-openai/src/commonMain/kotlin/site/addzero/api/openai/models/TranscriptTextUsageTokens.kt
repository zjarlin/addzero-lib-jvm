// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Usage statistics for models billed by token usage.
 */
@Serializable
data class TranscriptTextUsageTokens(
    /**
     * The type of the usage object. Always `tokens` for this variant.
     */
    val type: String,
    /**
     * Number of input tokens billed for this request.
     */
    @SerialName("input_tokens")
    val inputTokens: Int,
    /**
     * Details about the input tokens billed for this request.
     */
    @SerialName("input_token_details")
    val inputTokenDetails: site.addzero.api.openai.models.TranscriptTextUsageTokensInputTokenDetails? = null,
    /**
     * Number of output tokens generated.
     */
    @SerialName("output_tokens")
    val outputTokens: Int,
    /**
     * Total number of tokens used (input + output).
     */
    @SerialName("total_tokens")
    val totalTokens: Int
)
