// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when input audio transcription is configured, and a transcription request for a user
 * message failed. These events are separate from other `error` events so that the client can identify
 * the related Item.
 */
@Serializable
data class RealtimeServerEventConversationItemInputAudioTranscriptionFailed(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.input_audio_transcription.failed`.
     */
    val type: String,
    /**
     * The ID of the user message item.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the content part containing the audio.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * Details of the transcription error.
     */
    val error: site.addzero.api.openai.models.RealtimeServerEventConversationItemInputAudioTranscriptionFailedError
)
