// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The sampling parameters for the model.
 */
@Serializable
data class EvalGraderScoreModelSamplingParams(
    val seed: Int? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    val temperature: Double? = null,
    @SerialName("max_completions_tokens")
    val maxCompletionsTokens: Int? = null,
    @SerialName("reasoning_effort")
    val reasoningEffort: site.addzero.api.openai.models.ReasoningEffort? = null
)
