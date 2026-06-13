// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * File search tool call
 */
@Serializable
data class RunStepDetailsToolCallsFileSearchObject(
    /**
     * The ID of the tool call object.
     */
    val id: String,
    /**
     * The type of tool call. This is always going to be `file_search` for this type of tool call.
     */
    val type: String,
    /**
     * For now, this is always going to be an empty object.
     */
    @SerialName("file_search")
    val fileSearch: site.addzero.api.openai.models.RunStepDetailsToolCallsFileSearchObjectFileSearch
)
