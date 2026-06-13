// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON request body for image edits. Use `images` (array of `ImageRefParam`) instead of multipart
 * `image` uploads. You can reference images via external URLs, data URLs, or uploaded file IDs. JSON
 * edits support GPT image models only; DALL-E edits require multipart (`dall-e-2` only).
 */
@Serializable
data class EditImageBodyJsonParam(
    /**
     * The model to use for image editing.
     */
    val model: String? = "gpt-image-1.5",
    /**
     * Input image references to edit. For GPT image models, you can provide up to 16 images.
     */
    val images: List<site.addzero.api.openai.models.ImageRefParam>,
    val mask: site.addzero.api.openai.models.ImageRefParam? = null,
    /**
     * A text description of the desired image edit.
     */
    val prompt: String,
    /**
     * The number of edited images to generate.
     */
    val n: Int? = 1,
    /**
     * Output quality for GPT image models.
     */
    val quality: String? = "auto",
    /**
     * Controls fidelity to the original input image(s).
     */
    @SerialName("input_fidelity")
    val inputFidelity: String? = null,
    /**
     * Requested output image size.
     */
    val size: String? = "auto",
    /**
     * A unique identifier representing your end-user, which can help OpenAI monitor and detect abuse.
     */
    val user: String? = null,
    /**
     * Output image format. Supported for GPT image models.
     */
    @SerialName("output_format")
    val outputFormat: String? = "png",
    /**
     * Compression level for `jpeg` or `webp` output.
     */
    @SerialName("output_compression")
    val outputCompression: Int? = null,
    /**
     * Moderation level for GPT image models.
     */
    val moderation: String? = "auto",
    /**
     * Background behavior for generated image output.
     */
    val background: String? = "auto",
    /**
     * Stream partial image results as events.
     */
    val stream: Boolean? = false,
    @SerialName("partial_images")
    val partialImages: site.addzero.api.openai.models.PartialImages? = null
)
