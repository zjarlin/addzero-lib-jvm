// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Confirmation payload returned after unassigning a role.
 */
@Serializable
data class DeletedRoleAssignmentResource(
    /**
     * Identifier for the deleted assignment, such as `group.role.deleted` or `user.role.deleted`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Whether the assignment was removed.
     */
    val deleted: Boolean
)
