// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageContentTextAnnotationsFilePathObjectFilePath(
    /**
     * The ID of the file that was generated.
     */
    @SerialName("file_id")
    val fileId: String
)
