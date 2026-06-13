// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Specifies a tool the model should use. Use to force the model to call a specific tool.
 */
@Serializable
data class AssistantsNamedToolChoice(
    /**
     * The type of the tool. If type is `function`, the function name must be set
     */
    val type: String,
    val function: site.addzero.api.openai.models.AssistantsNamedToolChoiceFunction? = null
)
