// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Usage statistics related to the run. This value will be `null` if the run is not in a terminal state
 * (i.e. `in_progress`, `queued`, etc.).
 */
@Serializable
data class RunCompletionUsage2(
    /**
     * Number of completion tokens used over the course of the run.
     */
    @SerialName("completion_tokens")
    val completionTokens: Int,
    /**
     * Number of prompt tokens used over the course of the run.
     */
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    /**
     * Total number of tokens used (prompt + completion).
     */
    @SerialName("total_tokens")
    val totalTokens: Int
)
