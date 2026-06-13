// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * CreateEvalRequest
 */
@Serializable
data class CreateEvalRequest(
    /**
     * The name of the evaluation.
     */
    val name: String? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    /**
     * The configuration for the data source used for the evaluation runs. Dictates the schema of the data
     * used in the evaluation.
     */
    @SerialName("data_source_config")
    val dataSourceConfig: JsonElement,
    /**
     * A list of graders for all eval runs in this group. Graders can reference variables in the data
     * source using double curly braces notation, like `{{item.variable_name}}`. To reference the model's
     * output, use the `sample` namespace (ie, `{{sample.output_text}}`).
     */
    @SerialName("testing_criteria")
    val testingCriteria: List<JsonElement>
)
