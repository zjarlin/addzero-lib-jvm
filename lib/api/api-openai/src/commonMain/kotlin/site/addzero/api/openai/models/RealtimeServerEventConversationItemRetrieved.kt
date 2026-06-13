// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a conversation item is retrieved with `conversation.item.retrieve`. This is provided
 * as a way to fetch the server's representation of an item, for example to get access to the post-
 * processed audio data after noise cancellation and VAD. It includes the full content of the Item,
 * including audio data.
 */
@Serializable
data class RealtimeServerEventConversationItemRetrieved(
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
