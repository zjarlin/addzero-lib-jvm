// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Confirmation payload returned after deleting a group.
 */
@Serializable
data class GroupDeletedResource(
    /**
     * Always `group.deleted`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Identifier of the deleted group.
     */
    val id: String,
    /**
     * Whether the group was deleted.
     */
    val deleted: Boolean
)
