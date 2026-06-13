// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Send this event to truncate a previous assistant message’s audio. The server will produce audio
 * faster than realtime, so this event is useful when the user interrupts to truncate audio that has
 * already been sent to the client but not yet played. This will synchronize the server's understanding
 * of the audio with the client's playback. Truncating audio will delete the server-side text
 * transcript to ensure there is not text in the context that hasn't been heard by the user. If
 * successful, the server will respond with a `conversation.item.truncated` event.
 */
@Serializable
data class RealtimeBetaClientEventConversationItemTruncate(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `conversation.item.truncate`.
     */
    val type: String,
    /**
     * The ID of the assistant message item to truncate. Only assistant message items can be truncated.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the content part to truncate. Set this to 0.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * Inclusive duration up to which audio is truncated, in milliseconds. If the audio_end_ms is greater
     * than the actual audio duration, the server will respond with an error.
     */
    @SerialName("audio_end_ms")
    val audioEndMs: Int
)
