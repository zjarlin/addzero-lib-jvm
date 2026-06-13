// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A tool that allows the model to execute shell commands.
 */
@Serializable
data class FunctionShellToolParam(
    /**
     * The type of the shell tool. Always `shell`.
     */
    val type: String = "shell",
    val environment: JsonElement? = null
)
