// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Usage statistics related to the run step. This value will be `null` while the run step's status is
 * `in_progress`.
 */
@Serializable
data class RunStepCompletionUsage2(
    /**
     * Number of completion tokens used over the course of the run step.
     */
    @SerialName("completion_tokens")
    val completionTokens: Int,
    /**
     * Number of prompt tokens used over the course of the run step.
     */
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    /**
     * Total number of tokens used (prompt + completion).
     */
    @SerialName("total_tokens")
    val totalTokens: Int
)
