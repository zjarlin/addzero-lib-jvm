// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when an earlier assistant audio message item is truncated by the client with a
 * `conversation.item.truncate` event. This event is used to synchronize the server's understanding of
 * the audio with the client's playback. This action will truncate the audio and remove the server-side
 * text transcript to ensure there is no text in the context that hasn't been heard by the user.
 */
@Serializable
data class RealtimeServerEventConversationItemTruncated(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.truncated`.
     */
    val type: String,
    /**
     * The ID of the assistant message item that was truncated.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the content part that was truncated.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The duration up to which the audio was truncated, in milliseconds.
     */
    @SerialName("audio_end_ms")
    val audioEndMs: Int
)
