// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An image input block used within EvalItem content arrays.
 */
@Serializable
data class EvalItemInputImage(
    /**
     * The type of the image input. Always `input_image`.
     */
    val type: String,
    /**
     * The URL of the image input.
     */
    @SerialName("image_url")
    val imageUrl: String,
    /**
     * The detail level of the image to be sent to the model. One of `high`, `low`, or `auto`. Defaults to
     * `auto`.
     */
    val detail: String? = null
)
