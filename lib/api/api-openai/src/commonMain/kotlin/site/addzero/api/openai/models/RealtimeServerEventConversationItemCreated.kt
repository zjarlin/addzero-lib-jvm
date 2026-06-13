// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a conversation item is created. There are several scenarios that produce this event: -
 * The server is generating a Response, which if successful will produce either one or two Items, which
 * will be of type `message` (role `assistant`) or type `function_call`. - The input audio buffer has
 * been committed, either by the client or the server (in `server_vad` mode). The server will take the
 * content of the input audio buffer and add it to a new user message Item. - The client has sent a
 * `conversation.item.create` event to add a new Item to the Conversation.
 */
@Serializable
data class RealtimeServerEventConversationItemCreated(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.created`.
     */
    val type: String,
    @SerialName("previous_item_id")
    val previousItemId: String? = null,
    val item: site.addzero.api.openai.models.RealtimeConversationItem
)
