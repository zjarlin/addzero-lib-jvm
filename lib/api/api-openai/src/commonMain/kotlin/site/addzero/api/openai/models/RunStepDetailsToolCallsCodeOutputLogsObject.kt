// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Text output from the Code Interpreter tool call as part of a run step.
 */
@Serializable
data class RunStepDetailsToolCallsCodeOutputLogsObject(
    /**
     * Always `logs`.
     */
    val type: String,
    /**
     * The text output from the Code Interpreter tool call.
     */
    val logs: String
)
