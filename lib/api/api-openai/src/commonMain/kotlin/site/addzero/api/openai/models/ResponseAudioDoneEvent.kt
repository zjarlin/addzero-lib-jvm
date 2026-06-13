// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the audio response is complete.
 */
@Serializable
data class ResponseAudioDoneEvent(
    /**
     * The type of the event. Always `response.audio.done`.
     */
    val type: String,
    /**
     * The sequence number of the delta.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
