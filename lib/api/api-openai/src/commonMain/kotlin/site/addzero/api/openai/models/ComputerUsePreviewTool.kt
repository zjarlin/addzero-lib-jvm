// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A tool that controls a virtual computer. Learn more about the [computer
 * tool](https://platform.openai.com/docs/guides/tools-computer-use).
 */
@Serializable
data class ComputerUsePreviewTool(
    /**
     * The type of the computer use tool. Always `computer_use_preview`.
     */
    val type: String = "computer_use_preview",
    /**
     * The type of computer environment to control.
     */
    val environment: site.addzero.api.openai.models.ComputerEnvironment,
    /**
     * The width of the computer display.
     */
    @SerialName("display_width")
    val displayWidth: Int,
    /**
     * The height of the computer display.
     */
    @SerialName("display_height")
    val displayHeight: Int
)
