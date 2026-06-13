// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The input tokens detailed information for the image generation.
 */
@Serializable
data class ImagesUsageInputTokensDetails(
    /**
     * The number of text tokens in the input prompt.
     */
    @SerialName("text_tokens")
    val textTokens: Int,
    /**
     * The number of image tokens in the input prompt.
     */
    @SerialName("image_tokens")
    val imageTokens: Int
)
