// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * For now, this is always going to be an empty object.
 */
@Serializable
data class RunStepDetailsToolCallsFileSearchObjectFileSearch(
    @SerialName("ranking_options")
    val rankingOptions: site.addzero.api.openai.models.RunStepDetailsToolCallsFileSearchRankingOptionsObject? = null,
    /**
     * The results of the file search.
     */
    val results: List<site.addzero.api.openai.models.RunStepDetailsToolCallsFileSearchResultObject>? = null
)
