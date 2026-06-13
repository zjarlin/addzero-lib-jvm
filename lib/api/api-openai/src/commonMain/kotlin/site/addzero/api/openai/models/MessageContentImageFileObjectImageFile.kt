// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageContentImageFileObjectImageFile(
    /**
     * The [File](/docs/api-reference/files) ID of the image in the message content. Set `purpose="vision"`
     * when uploading the File if you need to later display the file content.
     */
    @SerialName("file_id")
    val fileId: String,
    /**
     * Specifies the detail level of the image if specified by the user. `low` uses fewer tokens, you can
     * opt in to high resolution using `high`.
     */
    val detail: String? = "auto"
)
