// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual user in a project.
 */
@Serializable
data class ProjectUser(
    /**
     * The object type, which is always `organization.project.user`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The name of the user
     */
    val name: String? = null,
    /**
     * The email address of the user
     */
    val email: String? = null,
    /**
     * `owner` or `member`
     */
    val role: String,
    /**
     * The Unix timestamp (in seconds) of when the project was added.
     */
    @SerialName("added_at")
    val addedAt: Long
)
