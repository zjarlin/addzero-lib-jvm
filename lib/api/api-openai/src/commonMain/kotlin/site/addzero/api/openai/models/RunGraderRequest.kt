// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * RunGraderRequest
 */
@Serializable
data class RunGraderRequest(
    /**
     * The grader used for the fine-tuning job.
     */
    val grader: JsonElement,
    /**
     * The dataset item provided to the grader. This will be used to populate the `item` namespace. See
     * [the guide](/docs/guides/graders) for more details.
     */
    val item: Map<String, JsonElement>? = null,
    /**
     * The model sample to be evaluated. This value will be used to populate the `sample` namespace. See
     * [the guide](/docs/guides/graders) for more details. The `output_json` variable will be populated if
     * the model sample is a valid JSON string.
     */
    @SerialName("model_sample")
    val modelSample: String
)
