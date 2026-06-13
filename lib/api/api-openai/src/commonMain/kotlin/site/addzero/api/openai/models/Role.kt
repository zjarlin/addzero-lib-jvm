// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details about a role that can be assigned through the public Roles API.
 */
@Serializable
data class Role(
    /**
     * Always `role`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Identifier for the role.
     */
    val id: String,
    /**
     * Unique name for the role.
     */
    val name: String,
    /**
     * Optional description of the role.
     */
    val description: String?,
    /**
     * Permissions granted by the role.
     */
    val permissions: List<String>,
    /**
     * Resource type the role is bound to (for example `api.organization` or `api.project`).
     */
    @SerialName("resource_type")
    val resourceType: String,
    /**
     * Whether the role is predefined and managed by OpenAI.
     */
    @SerialName("predefined_role")
    val predefinedRole: Boolean
)
