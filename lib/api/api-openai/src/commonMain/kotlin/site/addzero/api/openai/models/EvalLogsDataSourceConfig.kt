// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A LogsDataSourceConfig which specifies the metadata property of your logs query. This is usually
 * metadata like `usecase=chatbot` or `prompt-version=v2`, etc. The schema returned by this data source
 * config is used to defined what variables are available in your evals. `item` and `sample` are both
 * defined when using this data source config.
 */
@Serializable
data class EvalLogsDataSourceConfig(
    /**
     * The type of data source. Always `logs`.
     */
    val type: String = "logs",
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    /**
     * The json schema for the run data source items. Learn how to build JSON schemas [here](https://json-
     * schema.org/).
     */
    val schema: Map<String, JsonElement>
)
