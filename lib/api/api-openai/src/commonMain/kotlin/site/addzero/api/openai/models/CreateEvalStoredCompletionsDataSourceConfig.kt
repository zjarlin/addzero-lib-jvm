// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Deprecated in favor of LogsDataSourceConfig.
 */
@Serializable
data class CreateEvalStoredCompletionsDataSourceConfig(
    /**
     * The type of data source. Always `stored_completions`.
     */
    val type: String = "stored_completions",
    /**
     * Metadata filters for the stored completions data source.
     */
    val metadata: Map<String, JsonElement>? = null
)
