// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ranking options for search.
 */
@Serializable
data class VectorStoreSearchRequestRankingOptions(
    /**
     * Enable re-ranking; set to `none` to disable, which can help reduce latency.
     */
    val ranker: String? = "auto",
    @SerialName("score_threshold")
    val scoreThreshold: Double? = 0.0
)
