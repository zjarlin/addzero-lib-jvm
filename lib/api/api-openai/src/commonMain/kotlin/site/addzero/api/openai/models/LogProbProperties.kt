// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A log probability object.
 */
@Serializable
data class LogProbProperties(
    /**
     * The token that was used to generate the log probability.
     */
    val token: String,
    /**
     * The log probability of the token.
     */
    val logprob: Double,
    /**
     * The bytes that were used to generate the log probability.
     */
    val bytes: List<Int>
)
