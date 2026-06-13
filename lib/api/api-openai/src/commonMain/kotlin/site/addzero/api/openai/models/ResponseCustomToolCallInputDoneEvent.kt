// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Event indicating that input for a custom tool call is complete.
 */
@Serializable
data class ResponseCustomToolCallInputDoneEvent(
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
     * The index of the output this event applies to.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * Unique identifier for the API item associated with this event.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The complete input data for the custom tool call.
     */
    val input: String
)
