// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateImageEditRequest(
    /**
     * The image(s) to edit. Must be a supported image file or an array of images. For the GPT image models
     * (`gpt-image-1`, `gpt-image-1-mini`, and `gpt-image-1.5`), each image should be a `png`, `webp`, or
     * `jpg` file less than 50MB. You can provide up to 16 images. `chatgpt-image-latest` follows the same
     * input constraints as GPT image models. For `dall-e-2`, you can only provide one image, and it should
     * be a square `png` file less than 4MB.
     */
    val image: JsonElement,
    /**
     * A text description of the desired image(s). The maximum length is 1000 characters for `dall-e-2`,
     * and 32000 characters for the GPT image models.
     */
    val prompt: String,
    /**
     * An additional image whose fully transparent areas (e.g. where alpha is zero) indicate where `image`
     * should be edited. If there are multiple images provided, the mask will be applied on the first
     * image. Must be a valid PNG file, less than 4MB, and have the same dimensions as `image`.
     */
    val mask: ByteArray? = null,
    /**
     * Allows to set transparency for the background of the generated image(s). This parameter is only
     * supported for the GPT image models. Must be one of `transparent`, `opaque` or `auto` (default
     * value). When `auto` is used, the model will automatically determine the best background for the
     * image. If `transparent`, the output format needs to support transparency, so it should be set to
     * either `png` (default value) or `webp`.
     */
    val background: String? = "auto",
    /**
     * The model to use for image generation. Defaults to `gpt-image-1.5`.
     */
    val model: String? = "gpt-image-1.5",
    /**
     * The number of images to generate. Must be between 1 and 10.
     */
    val n: Int? = 1,
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
    val size: String? = "1024x1024",
    /**
     * The format in which the generated images are returned. Must be one of `url` or `b64_json`. URLs are
     * only valid for 60 minutes after the image has been generated. This parameter is only supported for
     * `dall-e-2` (default is `url` for `dall-e-2`), as GPT image models always return base64-encoded
     * images.
     */
    @SerialName("response_format")
    val responseFormat: String? = null,
    /**
     * The format in which the generated images are returned. This parameter is only supported for the GPT
     * image models. Must be one of `png`, `jpeg`, or `webp`. The default value is `png`.
     */
    @SerialName("output_format")
    val outputFormat: String? = "png",
    /**
     * The compression level (0-100%) for the generated images. This parameter is only supported for the
     * GPT image models with the `webp` or `jpeg` output formats, and defaults to 100.
     */
    @SerialName("output_compression")
    val outputCompression: Int? = 100,
    /**
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     * [Learn more](/docs/guides/safety-best-practices#end-user-ids).
     */
    val user: String? = null,
    @SerialName("input_fidelity")
    val inputFidelity: site.addzero.api.openai.models.InputFidelity? = null,
    /**
     * Edit the image in streaming mode. Defaults to `false`. See the [Image generation
     * guide](/docs/guides/image-generation) for more information.
     */
    val stream: Boolean? = false,
    @SerialName("partial_images")
    val partialImages: site.addzero.api.openai.models.PartialImages? = null,
    /**
     * The quality of the image that will be generated for GPT image models. Defaults to `auto`.
     */
    val quality: String? = "auto"
)
