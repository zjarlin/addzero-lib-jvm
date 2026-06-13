// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when image generation has completed and the final image is available.
 */
@Serializable
data class ImageGenCompletedEvent(
    /**
     * The type of the event. Always `image_generation.completed`.
     */
    val type: String,
    /**
     * Base64-encoded image data, suitable for rendering as an image.
     */
    @SerialName("b64_json")
    val b64Json: String,
    /**
     * The Unix timestamp when the event was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The size of the generated image.
     */
    val size: String,
    /**
     * The quality setting for the generated image.
     */
    val quality: String,
    /**
     * The background setting for the generated image.
     */
    val background: String,
    /**
     * The output format for the generated image.
     */
    @SerialName("output_format")
    val outputFormat: String,
    val usage: site.addzero.api.openai.models.ImagesUsage
)
