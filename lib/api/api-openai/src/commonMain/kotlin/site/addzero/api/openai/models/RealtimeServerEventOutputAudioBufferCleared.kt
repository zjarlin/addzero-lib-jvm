// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * **WebRTC/SIP Only:** Emitted when the output audio buffer is cleared. This happens either in VAD
 * mode when the user has interrupted (`input_audio_buffer.speech_started`), or when the client has
 * emitted the `output_audio_buffer.clear` event to manually cut off the current audio response. [Learn
 * more](/docs/guides/realtime-conversations#client-and-server-events-for-audio-in-webrtc).
 */
@Serializable
data class RealtimeServerEventOutputAudioBufferCleared(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `output_audio_buffer.cleared`.
     */
    val type: String,
    /**
     * The unique ID of the response that produced the audio.
     */
    @SerialName("response_id")
    val responseId: String
)
