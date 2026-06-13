// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The deleted conversation object
 */
@Serializable
data class DeletedConversation(
    @SerialName("object")
    val objectType: String = "conversation.deleted",
    val deleted: Boolean,
    val id: String
)
