// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The payload used to update the role.
 */
@Serializable
data class AuditLogRoleUpdatedChangesRequested(
    /**
     * The updated role name, when provided.
     */
    @SerialName("role_name")
    val roleName: String? = null,
    /**
     * The resource the role is scoped to.
     */
    @SerialName("resource_id")
    val resourceId: String? = null,
    /**
     * The type of resource the role belongs to.
     */
    @SerialName("resource_type")
    val resourceType: String? = null,
    /**
     * The permissions added to the role.
     */
    @SerialName("permissions_added")
    val permissionsAdded: List<String>? = null,
    /**
     * The permissions removed from the role.
     */
    @SerialName("permissions_removed")
    val permissionsRemoved: List<String>? = null,
    /**
     * The updated role description, when provided.
     */
    val description: String? = null,
    /**
     * Additional metadata stored on the role.
     */
    val metadata: Map<String, JsonElement>? = null
)
