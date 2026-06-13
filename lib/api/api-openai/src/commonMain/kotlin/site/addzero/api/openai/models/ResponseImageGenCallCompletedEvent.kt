// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when an image generation tool call has completed and the final image is available.
 */
@Serializable
data class ResponseImageGenCallCompletedEvent(
    /**
     * The type of the event. Always 'response.image_generation_call.completed'.
     */
    val type: String,
    /**
     * The index of the output item in the response's output array.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The unique identifier of the image generation item being processed.
     */
    @SerialName("item_id")
    val itemId: String
)
