// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Send this event when you want to retrieve the server's representation of a specific item in the
 * conversation history. This is useful, for example, to inspect user audio after noise cancellation
 * and VAD. The server will respond with a `conversation.item.retrieved` event, unless the item does
 * not exist in the conversation history, in which case the server will respond with an error.
 */
@Serializable
data class RealtimeClientEventConversationItemRetrieve(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `conversation.item.retrieve`.
     */
    val type: String,
    /**
     * The ID of the item to retrieve.
     */
    @SerialName("item_id")
    val itemId: String
)
