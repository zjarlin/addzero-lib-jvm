// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The conversation resource.
 */
@Serializable
data class RealtimeServerEventConversationCreatedConversation(
    /**
     * The unique ID of the conversation.
     */
    val id: String? = null,
    /**
     * The object type, must be `realtime.conversation`.
     */
    @SerialName("object")
    val objectType: String? = null
)
