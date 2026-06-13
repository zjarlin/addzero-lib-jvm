// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionTokenLogprob(
    /**
     * The token.
     */
    val token: String,
    /**
     * The log probability of this token, if it is within the top 20 most likely tokens. Otherwise, the
     * value `-9999.0` is used to signify that the token is very unlikely.
     */
    val logprob: Double,
    val bytes: List<Int>?,
    /**
     * List of the most likely tokens and their log probability, at this token position. The number of
     * entries may be fewer than the requested `top_logprobs`.
     */
    @SerialName("top_logprobs")
    val topLogprobs: List<site.addzero.api.openai.models.ChatCompletionTokenLogprobTopLogprob>
)
