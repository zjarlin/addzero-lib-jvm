// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The ranking options for the file search.
 */
@Serializable
data class RunStepDetailsToolCallsFileSearchRankingOptionsObject(
    val ranker: site.addzero.api.openai.models.FileSearchRanker,
    /**
     * The score threshold for the file search. All values must be a floating point number between 0 and 1.
     */
    @SerialName("score_threshold")
    val scoreThreshold: Double
)
