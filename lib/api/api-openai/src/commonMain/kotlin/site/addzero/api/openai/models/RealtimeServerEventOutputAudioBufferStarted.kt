// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * **WebRTC/SIP Only:** Emitted when the server begins streaming audio to the client. This event is
 * emitted after an audio content part has been added (`response.content_part.added`) to the response.
 * [Learn more](/docs/guides/realtime-conversations#client-and-server-events-for-audio-in-webrtc).
 */
@Serializable
data class RealtimeServerEventOutputAudioBufferStarted(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `output_audio_buffer.started`.
     */
    val type: String,
    /**
     * The unique ID of the response that produced the audio.
     */
    @SerialName("response_id")
    val responseId: String
)
