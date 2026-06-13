// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details on the action required to continue the run. Will be `null` if no action is required.
 */
@Serializable
data class RunObjectRequiredAction(
    /**
     * For now, this is always `submit_tool_outputs`.
     */
    val type: String,
    /**
     * Details on the tool outputs needed for this run to continue.
     */
    @SerialName("submit_tool_outputs")
    val submitToolOutputs: site.addzero.api.openai.models.RunObjectRequiredActionSubmitToolOutputs
)
