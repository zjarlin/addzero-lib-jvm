// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Overrides for the file search tool.
 */
@Serializable
data class AssistantToolsFileSearchFileSearch(
    /**
     * The maximum number of results the file search tool should output. The default is 20 for `gpt-4*`
     * models and 5 for `gpt-3.5-turbo`. This number should be between 1 and 50 inclusive. Note that the
     * file search tool may output fewer than `max_num_results` results. See the [file search tool
     * documentation](/docs/assistants/tools/file-search#customizing-file-search-settings) for more
     * information.
     */
    @SerialName("max_num_results")
    val maxNumResults: Int? = null,
    @SerialName("ranking_options")
    val rankingOptions: site.addzero.api.openai.models.FileSearchRankingOptions? = null
)
