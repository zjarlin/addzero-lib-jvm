// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details about the input tokens used in the Response. Cached tokens are tokens from previous turns in
 * the conversation that are included as context for the current response. Cached tokens here are
 * counted as a subset of input tokens, meaning input tokens will include cached and uncached tokens.
 */
@Serializable
data class RealtimeResponseUsageInputTokenDetails(
    /**
     * The number of cached tokens used as input for the Response.
     */
    @SerialName("cached_tokens")
    val cachedTokens: Int? = null,
    /**
     * The number of text tokens used as input for the Response.
     */
    @SerialName("text_tokens")
    val textTokens: Int? = null,
    /**
     * The number of image tokens used as input for the Response.
     */
    @SerialName("image_tokens")
    val imageTokens: Int? = null,
    /**
     * The number of audio tokens used as input for the Response.
     */
    @SerialName("audio_tokens")
    val audioTokens: Int? = null,
    /**
     * Details about the cached tokens used as input for the Response.
     */
    @SerialName("cached_tokens_details")
    val cachedTokensDetails: site.addzero.api.openai.models.RealtimeResponseUsageInputTokenDetailsCachedTokensDetails? = null
)
