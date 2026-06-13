// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A LabelModelGrader object which uses a model to assign labels to each item in the evaluation.
 */
@Serializable
data class GraderLabelModel(
    /**
     * The object type, which is always `label_model`.
     */
    val type: String,
    /**
     * The name of the grader.
     */
    val name: String,
    /**
     * The model to use for the evaluation. Must support structured outputs.
     */
    val model: String,
    val input: List<site.addzero.api.openai.models.EvalItem>,
    /**
     * The labels to assign to each item in the evaluation.
     */
    val labels: List<String>,
    /**
     * The labels that indicate a passing result. Must be a subset of labels.
     */
    @SerialName("passing_labels")
    val passingLabels: List<String>
)
