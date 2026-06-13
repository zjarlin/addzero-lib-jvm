// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A sample containing the input and output of the evaluation run.
 */
@Serializable
data class EvalRunOutputItemSample(
    /**
     * An array of input messages.
     */
    val input: List<site.addzero.api.openai.models.EvalRunOutputItemSampleInputItem>,
    /**
     * An array of output messages.
     */
    val output: List<site.addzero.api.openai.models.EvalRunOutputItemSampleOutputItem>,
    /**
     * The reason why the sample generation was finished.
     */
    @SerialName("finish_reason")
    val finishReason: String,
    /**
     * The model used for generating the sample.
     */
    val model: String,
    /**
     * Token usage details for the sample.
     */
    val usage: site.addzero.api.openai.models.EvalRunOutputItemSampleUsage,
    val error: site.addzero.api.openai.models.EvalApiError,
    /**
     * The sampling temperature used.
     */
    val temperature: Double,
    /**
     * The maximum number of tokens allowed for completion.
     */
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int,
    /**
     * The top_p value used for sampling.
     */
    @SerialName("top_p")
    val topP: Double,
    /**
     * The seed used for generating the sample.
     */
    val seed: Int
)
