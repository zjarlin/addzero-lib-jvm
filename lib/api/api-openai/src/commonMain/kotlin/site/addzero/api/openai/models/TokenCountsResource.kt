// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Token counts
 */
@Serializable
data class TokenCountsResource(
    @SerialName("object")
    val objectType: String = "response.input_tokens",
    @SerialName("input_tokens")
    val inputTokens: Int
)
