// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A data source config which specifies the metadata property of your logs query. This is usually
 * metadata like `usecase=chatbot` or `prompt-version=v2`, etc.
 */
@Serializable
data class CreateEvalLogsDataSourceConfig(
    /**
     * The type of data source. Always `logs`.
     */
    val type: String = "logs",
    /**
     * Metadata filters for the logs data source.
     */
    val metadata: Map<String, JsonElement>? = null
)
