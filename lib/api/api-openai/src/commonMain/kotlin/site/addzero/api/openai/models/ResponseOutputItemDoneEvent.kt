// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when an output item is marked done.
 */
@Serializable
data class ResponseOutputItemDoneEvent(
    /**
     * The type of the event. Always `response.output_item.done`.
     */
    val type: String,
    /**
     * The index of the output item that was marked done.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The output item that was marked done.
     */
    val item: site.addzero.api.openai.models.OutputItem
)
