// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A result instance of the file search.
 */
@Serializable
data class RunStepDetailsToolCallsFileSearchResultObject(
    /**
     * The ID of the file that result was found in.
     */
    @SerialName("file_id")
    val fileId: String,
    /**
     * The name of the file that result was found in.
     */
    @SerialName("file_name")
    val fileName: String,
    /**
     * The score of the result. All values must be a floating point number between 0 and 1.
     */
    val score: Double,
    /**
     * The content of the result that was found. The content is only included if requested via the include
     * query parameter.
     */
    val content: List<site.addzero.api.openai.models.RunStepDetailsToolCallsFileSearchResultObjectContentItem>? = null
)
