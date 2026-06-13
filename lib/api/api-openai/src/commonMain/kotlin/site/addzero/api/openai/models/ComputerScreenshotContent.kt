// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A screenshot of a computer.
 */
@Serializable
data class ComputerScreenshotContent(
    /**
     * Specifies the event type. For a computer screenshot, this property is always set to
     * `computer_screenshot`.
     */
    val type: String = "computer_screenshot",
    @SerialName("image_url")
    val imageUrl: String?,
    @SerialName("file_id")
    val fileId: String?,
    /**
     * The detail level of the screenshot image to be sent to the model. One of `high`, `low`, `auto`, or
     * `original`. Defaults to `auto`.
     */
    val detail: site.addzero.api.openai.models.ImageDetail
)
