// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when there is a partial transcript of audio.
 */
@Serializable
data class ResponseAudioTranscriptDeltaEvent(
    /**
     * The type of the event. Always `response.audio.transcript.delta`.
     */
    val type: String,
    /**
     * The partial transcript of the audio response.
     */
    val delta: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
