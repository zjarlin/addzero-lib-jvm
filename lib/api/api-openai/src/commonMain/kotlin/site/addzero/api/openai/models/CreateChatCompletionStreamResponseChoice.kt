// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateChatCompletionStreamResponseChoice(
    val delta: site.addzero.api.openai.models.ChatCompletionStreamResponseDelta,
    /**
     * Log probability information for the choice.
     */
    val logprobs: site.addzero.api.openai.models.CreateChatCompletionStreamResponseChoiceLogprobs? = null,
    /**
     * The reason the model stopped generating tokens. This will be `stop` if the model hit a natural stop
     * point or a provided stop sequence, `length` if the maximum number of tokens specified in the request
     * was reached, `content_filter` if content was omitted due to a flag from our content filters,
     * `tool_calls` if the model called a tool, or `function_call` (deprecated) if the model called a
     * function.
     */
    @SerialName("finish_reason")
    val finishReason: String?,
    /**
     * The index of the choice in the list of choices.
     */
    val index: Int
)
