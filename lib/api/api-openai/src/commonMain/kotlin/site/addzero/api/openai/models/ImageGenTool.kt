// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A tool that generates images using the GPT image models.
 */
@Serializable
data class ImageGenTool(
    /**
     * The type of the image generation tool. Always `image_generation`.
     */
    val type: String,
    val model: String? = null,
    /**
     * The quality of the generated image. One of `low`, `medium`, `high`, or `auto`. Default: `auto`.
     */
    val quality: String? = "auto",
    /**
     * The size of the generated images. For `gpt-image-2` and `gpt-image-2-2026-04-21`, arbitrary
     * resolutions are supported as `WIDTHxHEIGHT` strings, for example `1536x864`. Width and height must
     * both be divisible by 16 and the requested aspect ratio must be between 1:3 and 3:1. Resolutions
     * above `2560x1440` are experimental, and the maximum supported resolution is `3840x2160`. The
     * requested size must also satisfy the model's current pixel and edge limits. The standard sizes
     * `1024x1024`, `1536x1024`, and `1024x1536` are supported by the GPT image models; `auto` is supported
     * for models that allow automatic sizing. For `dall-e-2`, use one of `256x256`, `512x512`, or
     * `1024x1024`. For `dall-e-3`, use one of `1024x1024`, `1792x1024`, or `1024x1792`.
     */
    val size: String? = "auto",
    /**
     * The output format of the generated image. One of `png`, `webp`, or `jpeg`. Default: `png`.
     */
    @SerialName("output_format")
    val outputFormat: String? = "png",
    /**
     * Compression level for the output image. Default: 100.
     */
    @SerialName("output_compression")
    val outputCompression: Int? = 100,
    /**
     * Moderation level for the generated image. Default: `auto`.
     */
    val moderation: String? = "auto",
    /**
     * Background type for the generated image. One of `transparent`, `opaque`, or `auto`. Default: `auto`.
     */
    val background: String? = "auto",
    @SerialName("input_fidelity")
    val inputFidelity: site.addzero.api.openai.models.InputFidelity? = null,
    /**
     * Optional mask for inpainting. Contains `image_url` (string, optional) and `file_id` (string,
     * optional).
     */
    @SerialName("input_image_mask")
    val inputImageMask: site.addzero.api.openai.models.ImageGenToolInputImageMask? = null,
    /**
     * Number of partial images to generate in streaming mode, from 0 (default value) to 3.
     */
    @SerialName("partial_images")
    val partialImages: Int? = 0,
    /**
     * Whether to generate a new image or edit an existing image. Default: `auto`.
     */
    val action: site.addzero.api.openai.models.ImageGenActionEnum? = null
)
