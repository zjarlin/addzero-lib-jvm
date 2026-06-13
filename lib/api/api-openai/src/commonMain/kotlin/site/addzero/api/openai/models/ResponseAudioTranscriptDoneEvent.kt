// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the full audio transcript is completed.
 */
@Serializable
data class ResponseAudioTranscriptDoneEvent(
    /**
     * The type of the event. Always `response.audio.transcript.done`.
     */
    val type: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
