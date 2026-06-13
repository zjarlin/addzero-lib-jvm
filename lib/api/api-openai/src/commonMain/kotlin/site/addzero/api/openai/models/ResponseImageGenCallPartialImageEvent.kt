// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a partial image is available during image generation streaming.
 */
@Serializable
data class ResponseImageGenCallPartialImageEvent(
    /**
     * The type of the event. Always 'response.image_generation_call.partial_image'.
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
    val sequenceNumber: Int,
    /**
     * 0-based index for the partial image (backend is 1-based, but this is 0-based for the user).
     */
    @SerialName("partial_image_index")
    val partialImageIndex: Int,
    /**
     * Base64-encoded partial image data, suitable for rendering as an image.
     */
    @SerialName("partial_image_b64")
    val partialImageB64: String
)
