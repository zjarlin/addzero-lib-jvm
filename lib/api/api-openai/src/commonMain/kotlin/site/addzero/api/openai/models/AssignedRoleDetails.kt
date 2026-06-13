// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Detailed information about a role assignment entry returned when listing assignments.
 */
@Serializable
data class AssignedRoleDetails(
    /**
     * Identifier for the role.
     */
    val id: String,
    /**
     * Name of the role.
     */
    val name: String,
    /**
     * Permissions associated with the role.
     */
    val permissions: List<String>,
    /**
     * Resource type the role applies to.
     */
    @SerialName("resource_type")
    val resourceType: String,
    /**
     * Whether the role is predefined by OpenAI.
     */
    @SerialName("predefined_role")
    val predefinedRole: Boolean,
    /**
     * Description of the role.
     */
    val description: String?,
    /**
     * When the role was created.
     */
    @SerialName("created_at")
    val createdAt: Long?,
    /**
     * When the role was last updated.
     */
    @SerialName("updated_at")
    val updatedAt: Long?,
    /**
     * Identifier of the actor who created the role.
     */
    @SerialName("created_by")
    val createdBy: String?,
    /**
     * User details for the actor that created the role, when available.
     */
    @SerialName("created_by_user_obj")
    val createdByUserObj: Map<String, JsonElement>?,
    /**
     * Arbitrary metadata stored on the role.
     */
    val metadata: Map<String, JsonElement>?
)
