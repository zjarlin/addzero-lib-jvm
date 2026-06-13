// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Deprecated in favor of LogsDataSourceConfig.
 */
@Serializable
data class EvalStoredCompletionsDataSourceConfig(
    /**
     * The type of data source. Always `stored_completions`.
     */
    val type: String = "stored_completions",
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    /**
     * The json schema for the run data source items. Learn how to build JSON schemas [here](https://json-
     * schema.org/).
     */
    val schema: Map<String, JsonElement>
)
