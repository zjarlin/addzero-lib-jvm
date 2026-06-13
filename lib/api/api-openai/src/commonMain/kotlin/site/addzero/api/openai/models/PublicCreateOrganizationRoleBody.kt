// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for creating a custom role.
 */
@Serializable
data class PublicCreateOrganizationRoleBody(
    /**
     * Unique name for the role.
     */
    @SerialName("role_name")
    val roleName: String,
    /**
     * Permissions to grant to the role.
     */
    val permissions: List<String>,
    /**
     * Optional description of the role.
     */
    val description: String? = null
)
