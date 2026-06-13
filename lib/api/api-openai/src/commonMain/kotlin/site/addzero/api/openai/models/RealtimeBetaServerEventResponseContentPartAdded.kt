// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a new content part is added to an assistant message item during response generation.
 */
@Serializable
data class RealtimeBetaServerEventResponseContentPartAdded(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.content_part.added`.
     */
    val type: String,
    /**
     * The ID of the response.
     */
    @SerialName("response_id")
    val responseId: String,
    /**
     * The ID of the item to which the content part was added.
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
     * The content part that was added.
     */
    val part: site.addzero.api.openai.models.RealtimeBetaServerEventResponseContentPartAddedPart
)
