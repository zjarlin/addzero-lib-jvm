// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a partial image is available during image generation streaming.
 */
@Serializable
data class ImageGenPartialImageEvent(
    /**
     * The type of the event. Always `image_generation.partial_image`.
     */
    val type: String,
    /**
     * Base64-encoded partial image data, suitable for rendering as an image.
     */
    @SerialName("b64_json")
    val b64Json: String,
    /**
     * The Unix timestamp when the event was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The size of the requested image.
     */
    val size: String,
    /**
     * The quality setting for the requested image.
     */
    val quality: String,
    /**
     * The background setting for the requested image.
     */
    val background: String,
    /**
     * The output format for the requested image.
     */
    @SerialName("output_format")
    val outputFormat: String,
    /**
     * 0-based index for the partial image (streaming).
     */
    @SerialName("partial_image_index")
    val partialImageIndex: Int
)
