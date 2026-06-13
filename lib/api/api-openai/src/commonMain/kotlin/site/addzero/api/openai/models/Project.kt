// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual project.
 */
@Serializable
data class Project(
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The object type, which is always `organization.project`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The name of the project. This appears in reporting.
     */
    val name: String? = null,
    /**
     * The Unix timestamp (in seconds) of when the project was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("archived_at")
    val archivedAt: Long? = null,
    /**
     * `active` or `archived`
     */
    val status: String? = null,
    /**
     * The external key associated with the project.
     */
    @SerialName("external_key_id")
    val externalKeyId: String? = null
)
