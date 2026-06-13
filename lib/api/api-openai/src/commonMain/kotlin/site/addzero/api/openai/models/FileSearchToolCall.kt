// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The results of a file search tool call. See the [file search guide](/docs/guides/tools-file-search)
 * for more information.
 */
@Serializable
data class FileSearchToolCall(
    /**
     * The unique ID of the file search tool call.
     */
    val id: String,
    /**
     * The type of the file search tool call. Always `file_search_call`.
     */
    val type: String,
    /**
     * The status of the file search tool call. One of `in_progress`, `searching`, `incomplete` or
     * `failed`,
     */
    val status: String,
    /**
     * The queries used to search for files.
     */
    val queries: List<String>,
    val results: List<site.addzero.api.openai.models.FileSearchToolCallResult>? = null
)
