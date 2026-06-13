// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a conversation item is finalized. The event will include the full content of the Item
 * except for audio data, which can be retrieved separately with a `conversation.item.retrieve` event
 * if needed.
 */
@Serializable
data class RealtimeServerEventConversationItemDone(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.done`.
     */
    val type: String,
    @SerialName("previous_item_id")
    val previousItemId: String? = null,
    val item: site.addzero.api.openai.models.RealtimeConversationItem
)
