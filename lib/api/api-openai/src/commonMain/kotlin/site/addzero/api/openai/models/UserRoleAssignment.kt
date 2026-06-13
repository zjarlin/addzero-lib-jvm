// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Role assignment linking a user to a role.
 */
@Serializable
data class UserRoleAssignment(
    /**
     * Always `user.role`.
     */
    @SerialName("object")
    val objectType: String,
    val user: site.addzero.api.openai.models.User,
    val role: site.addzero.api.openai.models.Role
)
