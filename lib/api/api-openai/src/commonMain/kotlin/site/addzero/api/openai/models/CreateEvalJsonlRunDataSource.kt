// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A JsonlRunDataSource object with that specifies a JSONL file that matches the eval
 */
@Serializable
data class CreateEvalJsonlRunDataSource(
    /**
     * The type of data source. Always `jsonl`.
     */
    val type: String = "jsonl",
    /**
     * Determines what populates the `item` namespace in the data source.
     */
    val source: JsonElement
)
