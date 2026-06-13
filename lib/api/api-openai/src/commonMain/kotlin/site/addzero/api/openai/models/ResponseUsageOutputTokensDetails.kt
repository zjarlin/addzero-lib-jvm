// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A detailed breakdown of the output tokens.
 */
@Serializable
data class ResponseUsageOutputTokensDetails(
    /**
     * The number of reasoning tokens.
     */
    @SerialName("reasoning_tokens")
    val reasoningTokens: Int
)
