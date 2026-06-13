// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCompletionResponseChoice(
    /**
     * The reason the model stopped generating tokens. This will be `stop` if the model hit a natural stop
     * point or a provided stop sequence, `length` if the maximum number of tokens specified in the request
     * was reached, or `content_filter` if content was omitted due to a flag from our content filters.
     */
    @SerialName("finish_reason")
    val finishReason: String,
    val index: Int,
    val logprobs: site.addzero.api.openai.models.CreateCompletionResponseChoiceLogprobs?,
    val text: String
)
