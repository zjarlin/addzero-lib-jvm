// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ResponseLogProbTopLogprob(
    /**
     * A possible text token.
     */
    val token: String? = null,
    /**
     * The log probability of this token.
     */
    val logprob: Double? = null
)
