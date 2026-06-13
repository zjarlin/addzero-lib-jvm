// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionMessageToolCallChunk(
    val index: Int,
    /**
     * The ID of the tool call.
     */
    val id: String? = null,
    /**
     * The type of the tool. Currently, only `function` is supported.
     */
    val type: String? = null,
    val function: site.addzero.api.openai.models.ChatCompletionMessageToolCallChunkFunction? = null
)
