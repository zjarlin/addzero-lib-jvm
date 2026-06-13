// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Confirmation payload returned after deleting a role.
 */
@Serializable
data class RoleDeletedResource(
    /**
     * Always `role.deleted`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Identifier of the deleted role.
     */
    val id: String,
    /**
     * Whether the role was deleted.
     */
    val deleted: Boolean
)
