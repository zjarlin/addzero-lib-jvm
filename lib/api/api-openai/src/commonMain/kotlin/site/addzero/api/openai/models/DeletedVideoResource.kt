// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Confirmation payload returned after deleting a video.
 */
@Serializable
data class DeletedVideoResource(
    /**
     * The object type that signals the deletion response.
     */
    @SerialName("object")
    val objectType: String = "video.deleted",
    /**
     * Indicates that the video resource was deleted.
     */
    val deleted: Boolean,
    /**
     * Identifier of the deleted video.
     */
    val id: String
)
