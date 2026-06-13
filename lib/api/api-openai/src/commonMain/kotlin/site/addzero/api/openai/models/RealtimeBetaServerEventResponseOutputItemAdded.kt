// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a new Item is created during Response generation.
 */
@Serializable
data class RealtimeBetaServerEventResponseOutputItemAdded(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.output_item.added`.
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
