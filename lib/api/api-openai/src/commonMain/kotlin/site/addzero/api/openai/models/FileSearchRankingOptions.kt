// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The ranking options for the file search. If not specified, the file search tool will use the `auto`
 * ranker and a score_threshold of 0. See the [file search tool
 * documentation](/docs/assistants/tools/file-search#customizing-file-search-settings) for more
 * information.
 */
@Serializable
data class FileSearchRankingOptions(
    val ranker: site.addzero.api.openai.models.FileSearchRanker? = null,
    /**
     * The score threshold for the file search. All values must be a floating point number between 0 and 1.
     */
    @SerialName("score_threshold")
    val scoreThreshold: Double
)
