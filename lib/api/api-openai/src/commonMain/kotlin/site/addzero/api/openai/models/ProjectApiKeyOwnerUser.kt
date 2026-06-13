// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The user that owns a project API key.
 */
@Serializable
data class ProjectApiKeyOwnerUser(
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The email address of the user.
     */
    val email: String,
    /**
     * The name of the user.
     */
    val name: String,
    /**
     * The Unix timestamp (in seconds) of when the user was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The user's project role.
     */
    val role: String
)
