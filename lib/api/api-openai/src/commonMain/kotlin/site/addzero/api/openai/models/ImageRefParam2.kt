// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageRefParam2(
    /**
     * A fully qualified URL or base64-encoded data URL.
     */
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("file_id")
    val fileId: String? = null
)
