// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when image editing has completed and the final image is available.
 */
@Serializable
data class ImageEditCompletedEvent(
    /**
     * The type of the event. Always `image_edit.completed`.
     */
    val type: String,
    /**
     * Base64-encoded final edited image data, suitable for rendering as an image.
     */
    @SerialName("b64_json")
    val b64Json: String,
    /**
     * The Unix timestamp when the event was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The size of the edited image.
     */
    val size: String,
    /**
     * The quality setting for the edited image.
     */
    val quality: String,
    /**
     * The background setting for the edited image.
     */
    val background: String,
    /**
     * The output format for the edited image.
     */
    @SerialName("output_format")
    val outputFormat: String,
    val usage: site.addzero.api.openai.models.ImagesUsage
)
