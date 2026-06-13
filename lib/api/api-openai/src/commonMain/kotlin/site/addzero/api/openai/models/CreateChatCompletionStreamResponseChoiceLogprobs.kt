// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Log probability information for the choice.
 */
@Serializable
data class CreateChatCompletionStreamResponseChoiceLogprobs(
    /**
     * A list of message content tokens with log probability information.
     */
    val content: List<site.addzero.api.openai.models.ChatCompletionTokenLogprob>?,
    /**
     * A list of message refusal tokens with log probability information.
     */
    val refusal: List<site.addzero.api.openai.models.ChatCompletionTokenLogprob>?
)
