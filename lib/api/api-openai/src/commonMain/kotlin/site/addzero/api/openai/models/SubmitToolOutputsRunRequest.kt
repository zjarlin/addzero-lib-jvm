// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitToolOutputsRunRequest(
    /**
     * A list of tools for which the outputs are being submitted.
     */
    @SerialName("tool_outputs")
    val toolOutputs: List<site.addzero.api.openai.models.SubmitToolOutputsRunRequestToolOutput>,
    val stream: Boolean? = null
)
