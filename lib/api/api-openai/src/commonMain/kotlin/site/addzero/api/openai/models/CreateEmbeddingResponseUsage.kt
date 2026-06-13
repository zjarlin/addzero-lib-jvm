// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The usage information for the request.
 */
@Serializable
data class CreateEmbeddingResponseUsage(
    /**
     * The number of tokens used by the prompt.
     */
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    /**
     * The total number of tokens used by the request.
     */
    @SerialName("total_tokens")
    val totalTokens: Int
)
