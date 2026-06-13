// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A LabelModelGrader object which uses a model to assign labels to each item in the evaluation.
 */
@Serializable
data class CreateEvalLabelModelGrader(
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
    /**
     * A list of chat messages forming the prompt or context. May include variable references to the `item`
     * namespace, ie {{item.name}}.
     */
    val input: List<site.addzero.api.openai.models.CreateEvalItem>,
    /**
     * The labels to classify to each item in the evaluation.
     */
    val labels: List<String>,
    /**
     * The labels that indicate a passing result. Must be a subset of labels.
     */
    @SerialName("passing_labels")
    val passingLabels: List<String>
)
