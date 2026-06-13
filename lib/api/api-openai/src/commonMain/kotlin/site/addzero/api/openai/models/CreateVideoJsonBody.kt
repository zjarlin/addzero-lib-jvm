// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON parameters for creating a new video generation job.
 */
@Serializable
data class CreateVideoJsonBody(
    /**
     * The video generation model to use (allowed values: sora-2, sora-2-pro). Defaults to `sora-2`.
     */
    val model: site.addzero.api.openai.models.VideoModel? = null,
    /**
     * Text prompt that describes the video to generate.
     */
    val prompt: String,
    /**
     * Optional reference object that guides generation. Provide exactly one of `image_url` or `file_id`.
     */
    @SerialName("input_reference")
    val inputReference: site.addzero.api.openai.models.ImageRefParam2? = null,
    /**
     * Clip duration in seconds (allowed values: 4, 8, 12). Defaults to 4 seconds.
     */
    val seconds: site.addzero.api.openai.models.VideoSeconds? = null,
    /**
     * Output resolution formatted as width x height (allowed values: 720x1280, 1280x720, 1024x1792,
     * 1792x1024). Defaults to 720x1280.
     */
    val size: site.addzero.api.openai.models.VideoSize? = null
)
