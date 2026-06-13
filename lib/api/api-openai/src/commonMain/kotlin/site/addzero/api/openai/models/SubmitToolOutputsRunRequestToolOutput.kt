// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitToolOutputsRunRequestToolOutput(
    /**
     * The ID of the tool call in the `required_action` object within the run object the output is being
     * submitted for.
     */
    @SerialName("tool_call_id")
    val toolCallId: String? = null,
    /**
     * The output of the tool call to be submitted to continue the run.
     */
    val output: String? = null
)
