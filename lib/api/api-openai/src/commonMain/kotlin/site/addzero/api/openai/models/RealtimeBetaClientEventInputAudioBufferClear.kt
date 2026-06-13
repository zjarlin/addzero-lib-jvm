// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Send this event to clear the audio bytes in the buffer. The server will respond with an
 * `input_audio_buffer.cleared` event.
 */
@Serializable
data class RealtimeBetaClientEventInputAudioBufferClear(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `input_audio_buffer.clear`.
     */
    val type: String
)
