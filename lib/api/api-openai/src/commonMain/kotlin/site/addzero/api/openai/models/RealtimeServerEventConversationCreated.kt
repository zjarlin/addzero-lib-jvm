// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a conversation is created. Emitted right after session creation.
 */
@Serializable
data class RealtimeServerEventConversationCreated(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.created`.
     */
    val type: String,
    /**
     * The conversation resource.
     */
    val conversation: site.addzero.api.openai.models.RealtimeServerEventConversationCreatedConversation
)
