// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCompletionResponseChoiceLogprobs(
    @SerialName("text_offset")
    val textOffset: List<Int>? = null,
    @SerialName("token_logprobs")
    val tokenLogprobs: List<Double>? = null,
    val tokens: List<String>? = null,
    @SerialName("top_logprobs")
    val topLogprobs: List<Map<String, Double>>? = null
)
