// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when the text value of an "output_text" content part is done streaming. Also emitted when a
 * Response is interrupted, incomplete, or cancelled.
 */
@Serializable
data class RealtimeBetaServerEventResponseTextDone(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.output_text.done`.
     */
    val type: String,
    /**
     * The ID of the response.
     */
    @SerialName("response_id")
    val responseId: String,
    /**
     * The ID of the item.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item in the response.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the content part in the item's content array.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The final text content.
     */
    val text: String
)
