// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A TextSimilarityGrader object which grades text based on similarity metrics.
 */
@Serializable
data class GraderTextSimilarity(
    /**
     * The type of grader.
     */
    val type: String = "text_similarity",
    /**
     * The name of the grader.
     */
    val name: String,
    /**
     * The text being graded.
     */
    val input: String,
    /**
     * The text being graded against.
     */
    val reference: String,
    /**
     * The evaluation metric to use. One of `cosine`, `fuzzy_match`, `bleu`, `gleu`, `meteor`, `rouge_1`,
     * `rouge_2`, `rouge_3`, `rouge_4`, `rouge_5`, or `rouge_l`.
     */
    @SerialName("evaluation_metric")
    val evaluationMetric: String
)
