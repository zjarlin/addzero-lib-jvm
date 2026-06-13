// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a reasoning text is completed.
 */
@Serializable
data class ResponseReasoningTextDoneEvent(
    /**
     * The type of the event. Always `response.reasoning_text.done`.
     */
    val type: String,
    /**
     * The ID of the item this reasoning text is associated with.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item this reasoning text is associated with.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the reasoning content part.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The full text of the completed reasoning content.
     */
    val text: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
