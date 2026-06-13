// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Token usage details for the sample.
 */
@Serializable
data class EvalRunOutputItemSampleUsage(
    /**
     * The total number of tokens used.
     */
    @SerialName("total_tokens")
    val totalTokens: Int,
    /**
     * The number of completion tokens generated.
     */
    @SerialName("completion_tokens")
    val completionTokens: Int,
    /**
     * The number of prompt tokens used.
     */
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    /**
     * The number of tokens retrieved from cache.
     */
    @SerialName("cached_tokens")
    val cachedTokens: Int
)
