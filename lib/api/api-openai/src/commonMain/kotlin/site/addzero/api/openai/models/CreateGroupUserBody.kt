// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for adding a user to a group.
 */
@Serializable
data class CreateGroupUserBody(
    /**
     * Identifier of the user to add to the group.
     */
    @SerialName("user_id")
    val userId: String
)
