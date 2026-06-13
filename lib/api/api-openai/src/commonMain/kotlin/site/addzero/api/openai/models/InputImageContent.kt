// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An image input to the model. Learn about [image inputs](/docs/guides/vision).
 */
@Serializable
data class InputImageContent(
    /**
     * The type of the input item. Always `input_image`.
     */
    val type: String = "input_image",
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("file_id")
    val fileId: String? = null,
    /**
     * The detail level of the image to be sent to the model. One of `high`, `low`, `auto`, or `original`.
     * Defaults to `auto`.
     */
    val detail: site.addzero.api.openai.models.ImageDetail
)
