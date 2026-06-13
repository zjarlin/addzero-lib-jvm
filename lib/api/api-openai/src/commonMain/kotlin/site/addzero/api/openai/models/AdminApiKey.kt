// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual Admin API key in an org.
 */
@Serializable
data class AdminApiKey(
    /**
     * The object type, which is always `organization.admin_api_key`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The name of the API key
     */
    val name: String? = null,
    /**
     * The redacted value of the API key
     */
    @SerialName("redacted_value")
    val redactedValue: String,
    /**
     * The Unix timestamp (in seconds) of when the API key was created
     */
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("last_used_at")
    val lastUsedAt: Long? = null,
    val owner: site.addzero.api.openai.models.AdminApiKeyOwner
)
