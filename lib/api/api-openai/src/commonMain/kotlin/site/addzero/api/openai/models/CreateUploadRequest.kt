// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUploadRequest(
    /**
     * The name of the file to upload.
     */
    val filename: String,
    /**
     * The intended purpose of the uploaded file. See the [documentation on File purposes](/docs/api-
     * reference/files/create#files-create-purpose).
     */
    val purpose: String,
    /**
     * The number of bytes in the file you are uploading.
     */
    val bytes: Int,
    /**
     * The MIME type of the file. This must fall within the supported MIME types for your file purpose. See
     * the supported MIME types for assistants and vision.
     */
    @SerialName("mime_type")
    val mimeType: String,
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.FileExpirationAfter? = null
)
