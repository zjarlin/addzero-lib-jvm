// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A call to a custom tool created by the model.
 */
@Serializable
data class ChatCompletionMessageCustomToolCall(
    /**
     * The ID of the tool call.
     */
    val id: String,
    /**
     * The type of the tool. Always `custom`.
     */
    val type: String,
    /**
     * The custom tool that the model called.
     */
    val custom: site.addzero.api.openai.models.ChatCompletionMessageCustomToolCallCustom
)
