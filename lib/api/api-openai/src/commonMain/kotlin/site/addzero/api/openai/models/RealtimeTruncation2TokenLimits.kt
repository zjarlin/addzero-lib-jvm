// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Optional custom token limits for this truncation strategy. If not provided, the model's default
 * token limits will be used.
 */
@Serializable
data class RealtimeTruncation2TokenLimits(
    /**
     * Maximum tokens allowed in the conversation after instructions (which including tool definitions).
     * For example, setting this to 5,000 would mean that truncation would occur when the conversation
     * exceeds 5,000 tokens after instructions. This cannot be higher than the model's context window size
     * minus the maximum output tokens.
     */
    @SerialName("post_instructions")
    val postInstructions: Int? = null
)
