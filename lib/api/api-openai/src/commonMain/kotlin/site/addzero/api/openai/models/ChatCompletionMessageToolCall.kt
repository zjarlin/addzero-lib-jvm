// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A call to a function tool created by the model.
 */
@Serializable
data class ChatCompletionMessageToolCall(
    /**
     * The ID of the tool call.
     */
    val id: String,
    /**
     * The type of the tool. Currently, only `function` is supported.
     */
    val type: String,
    /**
     * The function that the model called.
     */
    val function: site.addzero.api.openai.models.ChatCompletionMessageToolCallFunction
)
