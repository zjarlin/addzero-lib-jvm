// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when there is a partial refusal text.
 */
@Serializable
data class ResponseRefusalDeltaEvent(
    /**
     * The type of the event. Always `response.refusal.delta`.
     */
    val type: String,
    /**
     * The ID of the output item that the refusal text is added to.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that the refusal text is added to.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the content part that the refusal text is added to.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The refusal text that is added.
     */
    val delta: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
