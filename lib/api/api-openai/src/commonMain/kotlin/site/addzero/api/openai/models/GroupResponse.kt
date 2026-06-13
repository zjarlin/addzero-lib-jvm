// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details about an organization group.
 */
@Serializable
data class GroupResponse(
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
     * Whether the group is managed through SCIM and controlled by your identity provider.
     */
    @SerialName("is_scim_managed")
    val isScimManaged: Boolean,
    /**
     * The type of the group.
     */
    @SerialName("group_type")
    val groupType: String
)
