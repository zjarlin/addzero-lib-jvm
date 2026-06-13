// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A computer screenshot image used with the computer use tool.
 */
@Serializable
data class ComputerScreenshotImage(
    /**
     * Specifies the event type. For a computer screenshot, this property is always set to
     * `computer_screenshot`.
     */
    val type: String = "computer_screenshot",
    /**
     * The URL of the screenshot image.
     */
    @SerialName("image_url")
    val imageUrl: String? = null,
    /**
     * The identifier of an uploaded file that contains the screenshot.
     */
    @SerialName("file_id")
    val fileId: String? = null
)
