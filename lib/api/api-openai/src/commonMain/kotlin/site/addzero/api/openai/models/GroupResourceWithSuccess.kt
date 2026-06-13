// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response returned after updating a group.
 */
@Serializable
data class GroupResourceWithSuccess(
    /**
     * Identifier for the group.
     */
    val id: String,
    /**
     * Updated display name for the group.
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
    val isScimManaged: Boolean
)
