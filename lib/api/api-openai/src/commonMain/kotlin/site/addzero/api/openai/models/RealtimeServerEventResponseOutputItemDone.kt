// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when an Item is done streaming. Also emitted when a Response is interrupted, incomplete, or
 * cancelled.
 */
@Serializable
data class RealtimeServerEventResponseOutputItemDone(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.output_item.done`.
     */
    val type: String,
    /**
     * The ID of the Response to which the item belongs.
     */
    @SerialName("response_id")
    val responseId: String,
    /**
     * The index of the output item in the Response.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    val item: site.addzero.api.openai.models.RealtimeConversationItem
)
