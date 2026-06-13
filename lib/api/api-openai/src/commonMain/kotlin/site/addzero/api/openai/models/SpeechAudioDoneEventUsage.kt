// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Token usage statistics for the request.
 */
@Serializable
data class SpeechAudioDoneEventUsage(
    /**
     * Number of input tokens in the prompt.
     */
    @SerialName("input_tokens")
    val inputTokens: Int,
    /**
     * Number of output tokens generated.
     */
    @SerialName("output_tokens")
    val outputTokens: Int,
    /**
     * Total number of tokens used (input + output).
     */
    @SerialName("total_tokens")
    val totalTokens: Int
)
