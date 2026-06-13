// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A source used in the search.
 */
@Serializable
data class WebSearchActionSearchSource(
    /**
     * The type of source. Always `url`.
     */
    val type: String,
    /**
     * The URL of the source.
     */
    val url: String
)
