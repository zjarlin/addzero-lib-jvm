// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateImageVariationRequest(
    /**
     * The image to use as the basis for the variation(s). Must be a valid PNG file, less than 4MB, and
     * square.
     */
    val image: ByteArray,
    /**
     * The model to use for image generation. Only `dall-e-2` is supported at this time.
     */
    val model: String? = "dall-e-2",
    /**
     * The number of images to generate. Must be between 1 and 10.
     */
    val n: Int? = 1,
    /**
     * The format in which the generated images are returned. Must be one of `url` or `b64_json`. URLs are
     * only valid for 60 minutes after the image has been generated.
     */
    @SerialName("response_format")
    val responseFormat: String? = "url",
    /**
     * The size of the generated images. Must be one of `256x256`, `512x512`, or `1024x1024`.
     */
    val size: String? = "1024x1024",
    /**
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     * [Learn more](/docs/guides/safety-best-practices#end-user-ids).
     */
    val user: String? = null
)
