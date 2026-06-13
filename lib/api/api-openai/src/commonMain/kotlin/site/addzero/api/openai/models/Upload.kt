// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The Upload object can accept byte chunks in the form of Parts.
 */
@Serializable
data class Upload(
    /**
     * The Upload unique identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The Unix timestamp (in seconds) for when the Upload was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The name of the file to be uploaded.
     */
    val filename: String,
    /**
     * The intended number of bytes to be uploaded.
     */
    val bytes: Int,
    /**
     * The intended purpose of the file. [Please refer here](/docs/api-reference/files/object#files/object-
     * purpose) for acceptable values.
     */
    val purpose: String,
    /**
     * The status of the Upload.
     */
    val status: String,
    /**
     * The Unix timestamp (in seconds) for when the Upload will expire.
     */
    @SerialName("expires_at")
    val expiresAt: Long,
    /**
     * The object type, which is always "upload".
     */
    @SerialName("object")
    val objectType: String? = null,
    val file: site.addzero.api.openai.models.UploadFile? = null
)
