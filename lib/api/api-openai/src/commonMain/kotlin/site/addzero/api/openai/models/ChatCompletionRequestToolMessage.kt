// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Tool message
 */
@Serializable
data class ChatCompletionRequestToolMessage(
    /**
     * The role of the messages author, in this case `tool`.
     */
    val role: String,
    /**
     * The contents of the tool message.
     */
    val content: JsonElement,
    /**
     * Tool call that this message is responding to.
     */
    @SerialName("tool_call_id")
    val toolCallId: String
)
