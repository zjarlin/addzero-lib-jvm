// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Details of the tool call.
 */
@Serializable
data class RunStepDetailsToolCallsObject(
    /**
     * Always `tool_calls`.
     */
    val type: String,
    /**
     * An array of tool calls the run step was involved in. These can be associated with one of three types
     * of tools: `code_interpreter`, `file_search`, or `function`.
     */
    @SerialName("tool_calls")
    val toolCalls: List<JsonElement>
)
