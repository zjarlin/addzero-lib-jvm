// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when text content is finalized.
 */
@Serializable
data class ResponseTextDoneEvent(
    /**
     * The type of the event. Always `response.output_text.done`.
     */
    val type: String,
    /**
     * The ID of the output item that the text content is finalized.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that the text content is finalized.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the content part that the text content is finalized.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The text content that is finalized.
     */
    val text: String,
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
