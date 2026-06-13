// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionTokenLogprobTopLogprob(
    /**
     * The token.
     */
    val token: String,
    /**
     * The log probability of this token, if it is within the top 20 most likely tokens. Otherwise, the
     * value `-9999.0` is used to signify that the token is very unlikely.
     */
    val logprob: Double,
    val bytes: List<Int>?
)
