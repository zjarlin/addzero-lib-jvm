// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A CustomDataSourceConfig which specifies the schema of your `item` and optionally `sample`
 * namespaces. The response schema defines the shape of the data that will be: - Used to define your
 * testing criteria and - What data is required when creating a run
 */
@Serializable
data class EvalCustomDataSourceConfig(
    /**
     * The type of data source. Always `custom`.
     */
    val type: String = "custom",
    /**
     * The json schema for the run data source items. Learn how to build JSON schemas [here](https://json-
     * schema.org/).
     */
    val schema: Map<String, JsonElement>
)
