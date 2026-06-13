// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An object describing an image to classify.
 */
@Serializable
data class CreateModerationRequestInputItem(
    /**
     * Always `image_url`.
     */
    val type: String,
    /**
     * Contains either an image URL or a data URL for a base64 encoded image.
     */
    @SerialName("image_url")
    val imageUrl: site.addzero.api.openai.models.CreateModerationRequestInputItemImageUrl
)
