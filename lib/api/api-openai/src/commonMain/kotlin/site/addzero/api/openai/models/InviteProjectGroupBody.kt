// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for granting a group access to a project.
 */
@Serializable
data class InviteProjectGroupBody(
    /**
     * Identifier of the group to add to the project.
     */
    @SerialName("group_id")
    val groupId: String,
    /**
     * Identifier of the project role to grant to the group.
     */
    val role: String
)
