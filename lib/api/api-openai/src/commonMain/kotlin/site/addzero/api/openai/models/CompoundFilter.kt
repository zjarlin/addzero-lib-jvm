// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Combine multiple filters using `and` or `or`.
 */
@Serializable
data class CompoundFilter(
    /**
     * Type of operation: `and` or `or`.
     */
    val type: String,
    /**
     * Array of filters to combine. Items can be `ComparisonFilter` or `CompoundFilter`.
     */
    val filters: List<JsonElement>
)
