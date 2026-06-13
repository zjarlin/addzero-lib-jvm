// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Messages sent by the model in response to user messages.
 */
@Serializable
data class ChatCompletionRequestAssistantMessage(
    val content: JsonElement? = null,
    val refusal: String? = null,
    /**
     * The role of the messages author, in this case `assistant`.
     */
    val role: String,
    /**
     * An optional name for the participant. Provides the model information to differentiate between
     * participants of the same role.
     */
    val name: String? = null,
    val audio: site.addzero.api.openai.models.ChatCompletionRequestAssistantMessageAudio? = null,
    @SerialName("tool_calls")
    val toolCalls: site.addzero.api.openai.models.ChatCompletionMessageToolCalls? = null,
    @SerialName("function_call")
    val functionCall: site.addzero.api.openai.models.ChatCompletionRequestAssistantMessageFunctionCall? = null
)
