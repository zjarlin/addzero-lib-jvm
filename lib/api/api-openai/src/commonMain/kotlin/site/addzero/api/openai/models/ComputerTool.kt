// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A tool that controls a virtual computer. Learn more about the [computer
 * tool](https://platform.openai.com/docs/guides/tools-computer-use).
 */
@Serializable
data class ComputerTool(
    /**
     * The type of the computer tool. Always `computer`.
     */
    val type: String = "computer"
)
