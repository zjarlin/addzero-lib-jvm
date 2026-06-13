// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A citation within the message that points to a specific quote from a specific File associated with
 * the assistant or the message. Generated when the assistant uses the "file_search" tool to search
 * files.
 */
@Serializable
data class MessageContentTextAnnotationsFileCitationObject(
    /**
     * Always `file_citation`.
     */
    val type: String,
    /**
     * The text in the message content that needs to be replaced.
     */
    val text: String,
    @SerialName("file_citation")
    val fileCitation: site.addzero.api.openai.models.MessageContentTextAnnotationsFileCitationObjectFileCitation,
    @SerialName("start_index")
    val startIndex: Int,
    @SerialName("end_index")
    val endIndex: Int
)
