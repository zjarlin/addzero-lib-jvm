// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A ResponsesRunDataSource object describing a model sampling configuration.
 */
@Serializable
data class CreateEvalResponsesRunDataSource(
    /**
     * The type of run data source. Always `responses`.
     */
    val type: String = "responses",
    /**
     * Used when sampling from a model. Dictates the structure of the messages passed into the model. Can
     * either be a reference to a prebuilt trajectory (ie, `item.input_trajectory`), or a template with
     * variable references to the `item` namespace.
     */
    @SerialName("input_messages")
    val inputMessages: JsonElement? = null,
    @SerialName("sampling_params")
    val samplingParams: site.addzero.api.openai.models.CreateEvalResponsesRunDataSourceSamplingParams? = null,
    /**
     * The name of the model to use for generating completions (e.g. "o3-mini").
     */
    val model: String? = null,
    /**
     * Determines what populates the `item` namespace in this run's data source.
     */
    val source: JsonElement
)
