// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for assigning a role to a group or user.
 */
@Serializable
data class PublicAssignOrganizationGroupRoleBody(
    /**
     * Identifier of the role to assign.
     */
    @SerialName("role_id")
    val roleId: String
)
