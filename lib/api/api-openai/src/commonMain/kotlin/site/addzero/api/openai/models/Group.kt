// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Summary information about a group returned in role assignment responses.
 */
@Serializable
data class Group(
    /**
     * Always `group`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Identifier for the group.
     */
    val id: String,
    /**
     * Display name of the group.
     */
    val name: String,
    /**
     * Unix timestamp (in seconds) when the group was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * Whether the group is managed through SCIM.
     */
    @SerialName("scim_managed")
    val scimManaged: Boolean
)
