// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned in `server_vad` mode when the server detects the end of speech in the audio buffer. The
 * server will also send an `conversation.item.created` event with the user message item that is
 * created from the audio buffer.
 */
@Serializable
data class RealtimeBetaServerEventInputAudioBufferSpeechStopped(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `input_audio_buffer.speech_stopped`.
     */
    val type: String,
    /**
     * Milliseconds since the session started when speech stopped. This will correspond to the end of audio
     * sent to the model, and thus includes the `min_silence_duration_ms` configured in the Session.
     */
    @SerialName("audio_end_ms")
    val audioEndMs: Int,
    /**
     * The ID of the user message item that will be created.
     */
    @SerialName("item_id")
    val itemId: String
)
