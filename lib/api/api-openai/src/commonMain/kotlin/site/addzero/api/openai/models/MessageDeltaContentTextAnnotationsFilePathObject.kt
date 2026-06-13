// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A URL for the file that's generated when the assistant used the `code_interpreter` tool to generate
 * a file.
 */
@Serializable
data class MessageDeltaContentTextAnnotationsFilePathObject(
    /**
     * The index of the annotation in the text content part.
     */
    val index: Int,
    /**
     * Always `file_path`.
     */
    val type: String,
    /**
     * The text in the message content that needs to be replaced.
     */
    val text: String? = null,
    @SerialName("file_path")
    val filePath: site.addzero.api.openai.models.MessageDeltaContentTextAnnotationsFilePathObjectFilePath? = null,
    @SerialName("start_index")
    val startIndex: Int? = null,
    @SerialName("end_index")
    val endIndex: Int? = null
)
