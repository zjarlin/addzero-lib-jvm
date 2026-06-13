// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when there is an additional text delta.
 */
@Serializable
data class ResponseTextDeltaEvent(
    /**
     * The type of the event. Always `response.output_text.delta`.
     */
    val type: String,
    /**
     * The ID of the output item that the text delta was added to.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that the text delta was added to.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the content part that the text delta was added to.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The text delta that was added.
     */
    val delta: String,
    /**
     * The sequence number for this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The log probabilities of the tokens in the delta.
     */
    val logprobs: List<site.addzero.api.openai.models.ResponseLogProb>
)
