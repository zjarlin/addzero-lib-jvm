// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The service account that owns a project API key.
 */
@Serializable
data class ProjectApiKeyOwnerServiceAccount(
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The name of the service account.
     */
    val name: String,
    /**
     * The Unix timestamp (in seconds) of when the service account was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The service account's project role.
     */
    val role: String
)
