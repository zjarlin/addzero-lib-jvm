// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A CustomDataSourceConfig object that defines the schema for the data source used for the evaluation
 * runs. This schema is used to define the shape of the data that will be: - Used to define your
 * testing criteria and - What data is required when creating a run
 */
@Serializable
data class CreateEvalCustomDataSourceConfig(
    /**
     * The type of data source. Always `custom`.
     */
    val type: String = "custom",
    /**
     * The json schema for each row in the data source.
     */
    @SerialName("item_schema")
    val itemSchema: Map<String, JsonElement>,
    /**
     * Whether the eval should expect you to populate the sample namespace (ie, by generating responses off
     * of your data source)
     */
    @SerialName("include_sample_schema")
    val includeSampleSchema: Boolean? = false
)
