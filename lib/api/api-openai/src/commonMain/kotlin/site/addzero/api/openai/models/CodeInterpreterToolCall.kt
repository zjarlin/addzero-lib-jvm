// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A tool call to run code.
 */
@Serializable
data class CodeInterpreterToolCall(
    /**
     * The type of the code interpreter tool call. Always `code_interpreter_call`.
     */
    val type: String = "code_interpreter_call",
    /**
     * The unique ID of the code interpreter tool call.
     */
    val id: String,
    /**
     * The status of the code interpreter tool call. Valid values are `in_progress`, `completed`,
     * `incomplete`, `interpreting`, and `failed`.
     */
    val status: String,
    /**
     * The ID of the container used to run the code.
     */
    @SerialName("container_id")
    val containerId: String,
    val code: String?,
    val outputs: List<JsonElement>?
)
