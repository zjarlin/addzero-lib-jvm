// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when there is a partial audio response.
 */
@Serializable
data class ResponseAudioDeltaEvent(
    /**
     * The type of the event. Always `response.audio.delta`.
     */
    val type: String,
    /**
     * A sequence number for this chunk of the stream response.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * A chunk of Base64 encoded response audio bytes.
     */
    val delta: String
)
