// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Confirmation payload returned after deleting a thread.
 */
@Serializable
data class DeletedThreadResource(
    /**
     * Identifier of the deleted thread.
     */
    val id: String,
    /**
     * Type discriminator that is always `chatkit.thread.deleted`.
     */
    @SerialName("object")
    val objectType: String = "chatkit.thread.deleted",
    /**
     * Indicates that the thread has been deleted.
     */
    val deleted: Boolean
)
