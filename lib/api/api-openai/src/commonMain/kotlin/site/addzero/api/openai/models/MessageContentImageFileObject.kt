// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * References an image [File](/docs/api-reference/files) in the content of a message.
 */
@Serializable
data class MessageContentImageFileObject(
    /**
     * Always `image_file`.
     */
    val type: String,
    @SerialName("image_file")
    val imageFile: site.addzero.api.openai.models.MessageContentImageFileObjectImageFile
)
