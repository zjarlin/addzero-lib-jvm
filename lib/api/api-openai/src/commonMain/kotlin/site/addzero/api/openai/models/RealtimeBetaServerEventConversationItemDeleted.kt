// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when an item in the conversation is deleted by the client with a `conversation.item.delete`
 * event. This event is used to synchronize the server's understanding of the conversation history with
 * the client's view.
 */
@Serializable
data class RealtimeBetaServerEventConversationItemDeleted(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.deleted`.
     */
    val type: String,
    /**
     * The ID of the item that was deleted.
     */
    @SerialName("item_id")
    val itemId: String
)
