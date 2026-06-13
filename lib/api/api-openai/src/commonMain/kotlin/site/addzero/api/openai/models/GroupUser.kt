// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Represents an individual user returned when inspecting group membership.
 */
@Serializable
data class GroupUser(
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The name of the user.
     */
    val name: String,
    /**
     * The email address of the user.
     */
    val email: String?
)
