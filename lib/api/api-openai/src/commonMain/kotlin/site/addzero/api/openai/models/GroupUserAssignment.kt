// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Confirmation payload returned after adding a user to a group.
 */
@Serializable
data class GroupUserAssignment(
    /**
     * Always `group.user`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Identifier of the user that was added.
     */
    @SerialName("user_id")
    val userId: String,
    /**
     * Identifier of the group the user was added to.
     */
    @SerialName("group_id")
    val groupId: String
)
