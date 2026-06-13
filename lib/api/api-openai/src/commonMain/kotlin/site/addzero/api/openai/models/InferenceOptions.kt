// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model and tool overrides applied when generating the assistant response.
 */
@Serializable
data class InferenceOptions(
    @SerialName("tool_choice")
    val toolChoice: site.addzero.api.openai.models.ToolChoice?,
    val model: String?
)
