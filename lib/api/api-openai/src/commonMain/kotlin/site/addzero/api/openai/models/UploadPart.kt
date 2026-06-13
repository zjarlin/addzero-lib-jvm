// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The upload Part represents a chunk of bytes we can add to an Upload object.
 */
@Serializable
data class UploadPart(
    /**
     * The upload Part unique identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The Unix timestamp (in seconds) for when the Part was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The ID of the Upload object that this Part was added to.
     */
    @SerialName("upload_id")
    val uploadId: String,
    /**
     * The object type, which is always `upload.part`.
     */
    @SerialName("object")
    val objectType: String
)
