// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual service account in a project.
 */
@Serializable
data class ProjectServiceAccount(
    /**
     * The object type, which is always `organization.project.service_account`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The name of the service account
     */
    val name: String,
    /**
     * `owner` or `member`
     */
    val role: String,
    /**
     * The Unix timestamp (in seconds) of when the service account was created
     */
    @SerialName("created_at")
    val createdAt: Long
)
