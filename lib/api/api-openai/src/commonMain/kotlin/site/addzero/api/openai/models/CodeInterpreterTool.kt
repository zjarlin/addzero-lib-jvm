// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A tool that runs Python code to help generate a response to a prompt.
 */
@Serializable
data class CodeInterpreterTool(
    /**
     * The type of the code interpreter tool. Always `code_interpreter`.
     */
    val type: String,
    /**
     * The code interpreter container. Can be a container ID or an object that specifies uploaded file IDs
     * to make available to your code, along with an optional `memory_limit` setting.
     */
    val container: JsonElement
)
