// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details on the tool outputs needed for this run to continue.
 */
@Serializable
data class RunObjectRequiredActionSubmitToolOutputs(
    /**
     * A list of the relevant tool calls.
     */
    @SerialName("tool_calls")
    val toolCalls: List<site.addzero.api.openai.models.RunToolCallObject>
)
