// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A detailed breakdown of the input tokens.
 */
@Serializable
data class BatchUsageInputTokensDetails(
    /**
     * The number of tokens that were retrieved from the cache. [More on prompt
     * caching](/docs/guides/prompt-caching).
     */
    @SerialName("cached_tokens")
    val cachedTokens: Int
)
