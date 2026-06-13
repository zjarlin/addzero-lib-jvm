// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * FileSearch tool
 */
@Serializable
data class AssistantToolsFileSearchTypeOnly(
    /**
     * The type of tool being defined: `file_search`
     */
    val type: String
)
