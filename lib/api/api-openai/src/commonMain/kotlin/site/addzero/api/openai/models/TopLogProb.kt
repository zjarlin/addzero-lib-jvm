// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The top log probability of a token.
 */
@Serializable
data class TopLogProb(
    val token: String,
    val logprob: Double,
    val bytes: List<Int>
)
