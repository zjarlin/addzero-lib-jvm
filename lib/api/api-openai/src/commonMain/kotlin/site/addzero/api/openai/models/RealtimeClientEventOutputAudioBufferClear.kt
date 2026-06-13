// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * **WebRTC/SIP Only:** Emit to cut off the current audio response. This will trigger the server to
 * stop generating audio and emit a `output_audio_buffer.cleared` event. This event should be preceded
 * by a `response.cancel` client event to stop the generation of the current response. [Learn
 * more](/docs/guides/realtime-conversations#client-and-server-events-for-audio-in-webrtc).
 */
@Serializable
data class RealtimeClientEventOutputAudioBufferClear(
    /**
     * The unique ID of the client event used for error handling.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `output_audio_buffer.clear`.
     */
    val type: String
)
