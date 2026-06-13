// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a delta is added to a reasoning text.
 */
@Serializable
data class ResponseReasoningTextDeltaEvent(
    /**
     * The type of the event. Always `response.reasoning_text.delta`.
     */
    val type: String,
    /**
     * The ID of the item this reasoning text delta is associated with.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item this reasoning text delta is associated with.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the reasoning content part this delta is associated with.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The text delta that was added to the reasoning content.
     */
    val delta: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
