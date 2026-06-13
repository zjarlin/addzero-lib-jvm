// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a reasoning summary text is completed.
 */
@Serializable
data class ResponseReasoningSummaryTextDoneEvent(
    /**
     * The type of the event. Always `response.reasoning_summary_text.done`.
     */
    val type: String,
    /**
     * The ID of the item this summary text is associated with.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item this summary text is associated with.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the summary part within the reasoning summary.
     */
    @SerialName("summary_index")
    val summaryIndex: Int,
    /**
     * The full text of the completed reasoning summary.
     */
    val text: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
