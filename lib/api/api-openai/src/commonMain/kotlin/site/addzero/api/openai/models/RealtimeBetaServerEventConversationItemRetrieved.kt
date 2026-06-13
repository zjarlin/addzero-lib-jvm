// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a conversation item is retrieved with `conversation.item.retrieve`.
 */
@Serializable
data class RealtimeBetaServerEventConversationItemRetrieved(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.retrieved`.
     */
    val type: String,
    val item: site.addzero.api.openai.models.RealtimeConversationItem
)
