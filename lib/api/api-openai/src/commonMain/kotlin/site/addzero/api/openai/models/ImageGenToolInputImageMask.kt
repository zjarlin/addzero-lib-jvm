// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Optional mask for inpainting. Contains `image_url` (string, optional) and `file_id` (string,
 * optional).
 */
@Serializable
data class ImageGenToolInputImageMask(
    /**
     * Base64-encoded mask image.
     */
    @SerialName("image_url")
    val imageUrl: String? = null,
    /**
     * File ID for the mask image.
     */
    @SerialName("file_id")
    val fileId: String? = null
)
