// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent by the server when an Item is added to the default Conversation. This can happen in several
 * cases: - When the client sends a `conversation.item.create` event. - When the input audio buffer is
 * committed. In this case the item will be a user message containing the audio from the buffer. - When
 * the model is generating a Response. In this case the `conversation.item.added` event will be sent
 * when the model starts generating a specific Item, and thus it will not yet have any content (and
 * `status` will be `in_progress`). The event will include the full content of the Item (except when
 * model is generating a Response) except for audio data, which can be retrieved separately with a
 * `conversation.item.retrieve` event if necessary.
 */
@Serializable
data class RealtimeServerEventConversationItemAdded(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.added`.
     */
    val type: String,
    @SerialName("previous_item_id")
    val previousItemId: String? = null,
    val item: site.addzero.api.openai.models.RealtimeConversationItem
)
