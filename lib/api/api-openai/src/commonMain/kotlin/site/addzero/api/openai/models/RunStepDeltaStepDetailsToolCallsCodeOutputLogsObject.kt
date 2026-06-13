// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Text output from the Code Interpreter tool call as part of a run step.
 */
@Serializable
data class RunStepDeltaStepDetailsToolCallsCodeOutputLogsObject(
    /**
     * The index of the output in the outputs array.
     */
    val index: Int,
    /**
     * Always `logs`.
     */
    val type: String,
    /**
     * The text output from the Code Interpreter tool call.
     */
    val logs: String? = null
)
