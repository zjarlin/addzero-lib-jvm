// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * **WebRTC/SIP Only:** Emitted when the output audio buffer has been completely drained on the server,
 * and no more audio is forthcoming. This event is emitted after the full response data has been sent
 * to the client (`response.done`). [Learn more](/docs/guides/realtime-conversations#client-and-server-
 * events-for-audio-in-webrtc).
 */
@Serializable
data class RealtimeServerEventOutputAudioBufferStopped(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `output_audio_buffer.stopped`.
     */
    val type: String,
    /**
     * The unique ID of the response that produced the audio.
     */
    @SerialName("response_id")
    val responseId: String
)
