// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A ScoreModelGrader object that uses a model to assign a score to the input.
 */
@Serializable
data class GraderScoreModel(
    /**
     * The object type, which is always `score_model`.
     */
    val type: String,
    /**
     * The name of the grader.
     */
    val name: String,
    /**
     * The model to use for the evaluation.
     */
    val model: String,
    /**
     * The sampling parameters for the model.
     */
    @SerialName("sampling_params")
    val samplingParams: site.addzero.api.openai.models.GraderScoreModelSamplingParams? = null,
    /**
     * The input messages evaluated by the grader. Supports text, output text, input image, and input audio
     * content blocks, and may include template strings.
     */
    val input: List<site.addzero.api.openai.models.EvalItem>,
    /**
     * The range of the score. Defaults to `[0, 1]`.
     */
    val range: List<Double>? = null
)
