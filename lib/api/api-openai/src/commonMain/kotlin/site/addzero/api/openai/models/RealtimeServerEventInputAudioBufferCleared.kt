// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when the input audio buffer is cleared by the client with a `input_audio_buffer.clear`
 * event.
 */
@Serializable
data class RealtimeServerEventInputAudioBufferCleared(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `input_audio_buffer.cleared`.
     */
    val type: String
)
