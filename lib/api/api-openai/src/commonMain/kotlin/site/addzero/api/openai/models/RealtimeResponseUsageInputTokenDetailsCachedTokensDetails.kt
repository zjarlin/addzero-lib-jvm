// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details about the cached tokens used as input for the Response.
 */
@Serializable
data class RealtimeResponseUsageInputTokenDetailsCachedTokensDetails(
    /**
     * The number of cached text tokens used as input for the Response.
     */
    @SerialName("text_tokens")
    val textTokens: Int? = null,
    /**
     * The number of cached image tokens used as input for the Response.
     */
    @SerialName("image_tokens")
    val imageTokens: Int? = null,
    /**
     * The number of cached audio tokens used as input for the Response.
     */
    @SerialName("audio_tokens")
    val audioTokens: Int? = null
)
