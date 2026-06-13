// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Log probability information for the choice.
 */
@Serializable
data class CreateChatCompletionResponseChoiceLogprobs(
    val content: List<site.addzero.api.openai.models.ChatCompletionTokenLogprob>?,
    val refusal: List<site.addzero.api.openai.models.ChatCompletionTokenLogprob>?
)
