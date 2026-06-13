// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Role assignment linking a group to a role.
 */
@Serializable
data class GroupRoleAssignment(
    /**
     * Always `group.role`.
     */
    @SerialName("object")
    val objectType: String,
    val group: site.addzero.api.openai.models.Group,
    val role: site.addzero.api.openai.models.Role
)
