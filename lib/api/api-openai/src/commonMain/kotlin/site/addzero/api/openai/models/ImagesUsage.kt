// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * For the GPT image models only, the token usage information for the image generation.
 */
@Serializable
data class ImagesUsage(
    /**
     * The total number of tokens (images and text) used for the image generation.
     */
    @SerialName("total_tokens")
    val totalTokens: Int,
    /**
     * The number of tokens (images and text) in the input prompt.
     */
    @SerialName("input_tokens")
    val inputTokens: Int,
    /**
     * The number of image tokens in the output image.
     */
    @SerialName("output_tokens")
    val outputTokens: Int,
    /**
     * The input tokens detailed information for the image generation.
     */
    @SerialName("input_tokens_details")
    val inputTokensDetails: site.addzero.api.openai.models.ImagesUsageInputTokensDetails
)
