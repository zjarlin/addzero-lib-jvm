// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The Code Interpreter tool call definition.
 */
@Serializable
data class RunStepDeltaStepDetailsToolCallsCodeObjectCodeInterpreter(
    /**
     * The input to the Code Interpreter tool call.
     */
    val input: String? = null,
    /**
     * The outputs from the Code Interpreter tool call. Code Interpreter can output one or more items,
     * including text (`logs`) or images (`image`). Each of these are represented by a different object
     * type.
     */
    val outputs: List<JsonElement>? = null
)
