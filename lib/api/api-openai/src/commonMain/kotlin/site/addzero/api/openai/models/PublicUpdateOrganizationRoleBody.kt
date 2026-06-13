// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for updating an existing role.
 */
@Serializable
data class PublicUpdateOrganizationRoleBody(
    /**
     * Updated set of permissions for the role.
     */
    val permissions: List<String>? = null,
    /**
     * New description for the role.
     */
    val description: String? = null,
    /**
     * New name for the role.
     */
    @SerialName("role_name")
    val roleName: String? = null
)
