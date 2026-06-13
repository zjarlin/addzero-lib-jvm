// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Add a new Item to the Conversation's context, including messages, function calls, and function call
 * responses. This event can be used both to populate a "history" of the conversation and to add new
 * items mid-stream, but has the current limitation that it cannot populate assistant audio messages.
 * If successful, the server will respond with a `conversation.item.created` event, otherwise an
 * `error` event will be sent.
 */
@Serializable
data class RealtimeClientEventConversationItemCreate(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `conversation.item.create`.
     */
    val type: String,
    /**
     * The ID of the preceding item after which the new item will be inserted. If not set, the new item
     * will be appended to the end of the conversation. If set to `root`, the new item will be added to the
     * beginning of the conversation. If set to an existing ID, it allows an item to be inserted mid-
     * conversation. If the ID cannot be found, an error will be returned and the item will not be added.
     */
    @SerialName("previous_item_id")
    val previousItemId: String? = null,
    val item: site.addzero.api.openai.models.RealtimeConversationItem
)
