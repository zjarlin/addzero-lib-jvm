// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A tool that searches for relevant content from uploaded files. Learn more about the [file search
 * tool](https://platform.openai.com/docs/guides/tools-file-search).
 */
@Serializable
data class FileSearchTool(
    /**
     * The type of the file search tool. Always `file_search`.
     */
    val type: String = "file_search",
    /**
     * The IDs of the vector stores to search.
     */
    @SerialName("vector_store_ids")
    val vectorStoreIds: List<String>,
    /**
     * The maximum number of results to return. This number should be between 1 and 50 inclusive.
     */
    @SerialName("max_num_results")
    val maxNumResults: Int? = null,
    /**
     * Ranking options for search.
     */
    @SerialName("ranking_options")
    val rankingOptions: site.addzero.api.openai.models.RankingOptions? = null,
    val filters: site.addzero.api.openai.models.Filters? = null
)
