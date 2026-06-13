// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a delta is added to a reasoning summary text.
 */
@Serializable
data class ResponseReasoningSummaryTextDeltaEvent(
    /**
     * The type of the event. Always `response.reasoning_summary_text.delta`.
     */
    val type: String,
    /**
     * The ID of the item this summary text delta is associated with.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item this summary text delta is associated with.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the summary part within the reasoning summary.
     */
    @SerialName("summary_index")
    val summaryIndex: Int,
    /**
     * The text delta that was added to the summary.
     */
    val delta: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
