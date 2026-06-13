// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A filter used to compare a specified attribute key to a given value using a defined comparison
 * operation.
 */
@Serializable
data class ComparisonFilter(
    /**
     * Specifies the comparison operator: `eq`, `ne`, `gt`, `gte`, `lt`, `lte`, `in`, `nin`. - `eq`: equals
     * - `ne`: not equal - `gt`: greater than - `gte`: greater than or equal - `lt`: less than - `lte`:
     * less than or equal - `in`: in - `nin`: not in
     */
    val type: String = "eq",
    /**
     * The key to compare against the value.
     */
    val key: String,
    /**
     * The value to compare against the attribute key; supports string, number, or boolean types.
     */
    val value: JsonElement
)
