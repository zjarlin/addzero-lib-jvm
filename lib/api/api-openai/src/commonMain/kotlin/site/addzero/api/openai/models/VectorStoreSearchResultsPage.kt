// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VectorStoreSearchResultsPage(
    /**
     * The object type, which is always `vector_store.search_results.page`
     */
    @SerialName("object")
    val objectType: String,
    @SerialName("search_query")
    val searchQuery: List<String>,
    /**
     * The list of search result items.
     */
    val data: List<site.addzero.api.openai.models.VectorStoreSearchResultItem>,
    /**
     * Indicates if there are more results to fetch.
     */
    @SerialName("has_more")
    val hasMore: Boolean,
    @SerialName("next_page")
    val nextPage: String?
)
