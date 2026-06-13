// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Specifies a tool the model should use. Use to force the model to call a specific function.
 */
@Serializable
data class ChatCompletionNamedToolChoice(
    /**
     * For function calling, the type is always `function`.
     */
    val type: String,
    val function: site.addzero.api.openai.models.ChatCompletionNamedToolChoiceFunction
)
