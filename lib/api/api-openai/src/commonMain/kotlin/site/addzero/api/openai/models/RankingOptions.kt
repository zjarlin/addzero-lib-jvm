// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RankingOptions(
    /**
     * The ranker to use for the file search.
     */
    val ranker: site.addzero.api.openai.models.RankerVersionType? = null,
    /**
     * The score threshold for the file search, a number between 0 and 1. Numbers closer to 1 will attempt
     * to return only the most relevant results, but may return fewer results.
     */
    @SerialName("score_threshold")
    val scoreThreshold: Double? = null,
    /**
     * Weights that control how reciprocal rank fusion balances semantic embedding matches versus sparse
     * keyword matches when hybrid search is enabled.
     */
    @SerialName("hybrid_search")
    val hybridSearch: site.addzero.api.openai.models.HybridSearchOptions? = null
)
