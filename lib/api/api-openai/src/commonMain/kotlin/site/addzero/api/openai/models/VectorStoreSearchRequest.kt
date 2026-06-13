// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class VectorStoreSearchRequest(
    /**
     * A query string for a search
     */
    val query: JsonElement,
    /**
     * Whether to rewrite the natural language query for vector search.
     */
    @SerialName("rewrite_query")
    val rewriteQuery: Boolean? = false,
    /**
     * The maximum number of results to return. This number should be between 1 and 50 inclusive.
     */
    @SerialName("max_num_results")
    val maxNumResults: Int? = 10,
    /**
     * A filter to apply based on file attributes.
     */
    val filters: JsonElement? = null,
    /**
     * Ranking options for search.
     */
    @SerialName("ranking_options")
    val rankingOptions: site.addzero.api.openai.models.VectorStoreSearchRequestRankingOptions? = null
)
