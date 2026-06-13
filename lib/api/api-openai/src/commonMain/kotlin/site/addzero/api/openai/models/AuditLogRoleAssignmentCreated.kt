// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogRoleAssignmentCreated(
    /**
     * The identifier of the role assignment.
     */
    val id: String? = null,
    /**
     * The principal (user or group) that received the role.
     */
    @SerialName("principal_id")
    val principalId: String? = null,
    /**
     * The type of principal (user or group) that received the role.
     */
    @SerialName("principal_type")
    val principalType: String? = null,
    /**
     * The resource the role assignment is scoped to.
     */
    @SerialName("resource_id")
    val resourceId: String? = null,
    /**
     * The type of resource the role assignment is scoped to.
     */
    @SerialName("resource_type")
    val resourceType: String? = null
)
