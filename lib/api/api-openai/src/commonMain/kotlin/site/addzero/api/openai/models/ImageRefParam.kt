// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Reference an input image by either URL or uploaded file ID. Provide exactly one of `image_url` or
 * `file_id`.
 */
@Serializable
data class ImageRefParam(
    /**
     * A fully qualified URL or base64-encoded data URL.
     */
    @SerialName("image_url")
    val imageUrl: String? = null,
    /**
     * The File API ID of an uploaded image to use as input.
     */
    @SerialName("file_id")
    val fileId: String? = null
)
