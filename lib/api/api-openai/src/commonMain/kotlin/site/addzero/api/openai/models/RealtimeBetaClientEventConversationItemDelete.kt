// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Send this event when you want to remove any item from the conversation history. The server will
 * respond with a `conversation.item.deleted` event, unless the item does not exist in the conversation
 * history, in which case the server will respond with an error.
 */
@Serializable
data class RealtimeBetaClientEventConversationItemDelete(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `conversation.item.delete`.
     */
    val type: String,
    /**
     * The ID of the item to delete.
     */
    @SerialName("item_id")
    val itemId: String
)
