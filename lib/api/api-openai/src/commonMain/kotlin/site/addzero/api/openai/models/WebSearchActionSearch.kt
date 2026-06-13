// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Action type "search" - Performs a web search query.
 */
@Serializable
data class WebSearchActionSearch(
    /**
     * The action type.
     */
    val type: String,
    /**
     * [DEPRECATED] The search query.
     */
    val query: String,
    /**
     * The search queries.
     */
    val queries: List<String>? = null,
    /**
     * The sources used in the search.
     */
    val sources: List<site.addzero.api.openai.models.WebSearchActionSearchSource>? = null
)
