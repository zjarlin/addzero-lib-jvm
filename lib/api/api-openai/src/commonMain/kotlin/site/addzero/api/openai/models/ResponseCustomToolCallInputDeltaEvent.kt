// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Event representing a delta (partial update) to the input of a custom tool call.
 */
@Serializable
data class ResponseCustomToolCallInputDeltaEvent(
    /**
     * The event type identifier.
     */
    val type: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The index of the output this delta applies to.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * Unique identifier for the API item associated with this event.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The incremental input data (delta) for the custom tool call.
     */
    val delta: String
)
