// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * References an image URL in the content of a message.
 */
@Serializable
data class MessageContentImageUrlObject(
    /**
     * The type of the content part.
     */
    val type: String,
    @SerialName("image_url")
    val imageUrl: site.addzero.api.openai.models.MessageContentImageUrlObjectImageUrl
)
