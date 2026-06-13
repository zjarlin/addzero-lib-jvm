// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * FileSearch tool
 */
@Serializable
data class AssistantToolsFileSearch(
    /**
     * The type of tool being defined: `file_search`
     */
    val type: String,
    /**
     * Overrides for the file search tool.
     */
    @SerialName("file_search")
    val fileSearch: site.addzero.api.openai.models.AssistantToolsFileSearchFileSearch? = null
)
