// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when an image generation tool call is actively generating an image (intermediate state).
 */
@Serializable
data class ResponseImageGenCallGeneratingEvent(
    /**
     * The type of the event. Always 'response.image_generation_call.generating'.
     */
    val type: String,
    /**
     * The index of the output item in the response's output array.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The unique identifier of the image generation item being processed.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The sequence number of the image generation item being processed.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
