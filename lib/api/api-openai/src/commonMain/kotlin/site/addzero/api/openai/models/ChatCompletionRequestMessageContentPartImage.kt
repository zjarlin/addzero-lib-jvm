// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Learn about [image inputs](/docs/guides/vision).
 */
@Serializable
data class ChatCompletionRequestMessageContentPartImage(
    /**
     * The type of the content part.
     */
    val type: String,
    @SerialName("image_url")
    val imageUrl: site.addzero.api.openai.models.ChatCompletionRequestMessageContentPartImageImageUrl
)
