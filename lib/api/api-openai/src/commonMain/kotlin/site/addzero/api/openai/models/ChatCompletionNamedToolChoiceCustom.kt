// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Specifies a tool the model should use. Use to force the model to call a specific custom tool.
 */
@Serializable
data class ChatCompletionNamedToolChoiceCustom(
    /**
     * For custom tool calling, the type is always `custom`.
     */
    val type: String,
    val custom: site.addzero.api.openai.models.ChatCompletionNamedToolChoiceCustomCustom
)
