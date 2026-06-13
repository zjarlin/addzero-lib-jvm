// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Constrains the tools available to the model to a pre-defined set.
 */
@Serializable
data class ChatCompletionAllowedToolsChoice(
    /**
     * Allowed tool configuration type. Always `allowed_tools`.
     */
    val type: String,
    @SerialName("allowed_tools")
    val allowedTools: site.addzero.api.openai.models.ChatCompletionAllowedTools
)
