// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogRoleCreated(
    /**
     * The role ID.
     */
    val id: String? = null,
    /**
     * The name of the role.
     */
    @SerialName("role_name")
    val roleName: String? = null,
    /**
     * The permissions granted by the role.
     */
    val permissions: List<String>? = null,
    /**
     * The type of resource the role belongs to.
     */
    @SerialName("resource_type")
    val resourceType: String? = null,
    /**
     * The resource the role is scoped to.
     */
    @SerialName("resource_id")
    val resourceId: String? = null
)
