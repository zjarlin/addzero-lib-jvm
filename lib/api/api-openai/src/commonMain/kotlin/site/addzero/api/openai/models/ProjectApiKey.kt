// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual API key in a project.
 */
@Serializable
data class ProjectApiKey(
    /**
     * The object type, which is always `organization.project.api_key`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The redacted value of the API key
     */
    @SerialName("redacted_value")
    val redactedValue: String,
    /**
     * The name of the API key
     */
    val name: String,
    /**
     * The Unix timestamp (in seconds) of when the API key was created
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The Unix timestamp (in seconds) of when the API key was last used.
     */
    @SerialName("last_used_at")
    val lastUsedAt: Long?,
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    val owner: site.addzero.api.openai.models.ProjectApiKeyOwner
)
