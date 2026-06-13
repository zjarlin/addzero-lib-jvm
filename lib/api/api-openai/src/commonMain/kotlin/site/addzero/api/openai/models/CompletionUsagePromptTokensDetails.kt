// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Breakdown of tokens used in the prompt.
 */
@Serializable
data class CompletionUsagePromptTokensDetails(
    /**
     * Audio input tokens present in the prompt.
     */
    @SerialName("audio_tokens")
    val audioTokens: Int? = 0,
    /**
     * Cached tokens present in the prompt.
     */
    @SerialName("cached_tokens")
    val cachedTokens: Int? = 0
)
