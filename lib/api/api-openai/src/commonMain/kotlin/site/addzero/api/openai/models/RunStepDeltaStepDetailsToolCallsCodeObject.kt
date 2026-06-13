// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details of the Code Interpreter tool call the run step was involved in.
 */
@Serializable
data class RunStepDeltaStepDetailsToolCallsCodeObject(
    /**
     * The index of the tool call in the tool calls array.
     */
    val index: Int,
    /**
     * The ID of the tool call.
     */
    val id: String? = null,
    /**
     * The type of tool call. This is always going to be `code_interpreter` for this type of tool call.
     */
    val type: String,
    /**
     * The Code Interpreter tool call definition.
     */
    @SerialName("code_interpreter")
    val codeInterpreter: site.addzero.api.openai.models.RunStepDeltaStepDetailsToolCallsCodeObjectCodeInterpreter? = null
)
