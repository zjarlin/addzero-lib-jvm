// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The response from the image generation endpoint.
 */
@Serializable
data class ImagesResponse(
    /**
     * The Unix timestamp (in seconds) of when the image was created.
     */
    val created: Long,
    /**
     * The list of generated images.
     */
    val data: List<site.addzero.api.openai.models.Image>? = null,
    /**
     * The background parameter used for the image generation. Either `transparent` or `opaque`.
     */
    val background: String? = null,
    /**
     * The output format of the image generation. Either `png`, `webp`, or `jpeg`.
     */
    @SerialName("output_format")
    val outputFormat: String? = null,
    /**
     * The size of the image generated. Either `1024x1024`, `1024x1536`, or `1536x1024`.
     */
    val size: String? = null,
    /**
     * The quality of the image generated. Either `low`, `medium`, or `high`.
     */
    val quality: String? = null,
    val usage: site.addzero.api.openai.models.ImageGenUsage? = null
)
