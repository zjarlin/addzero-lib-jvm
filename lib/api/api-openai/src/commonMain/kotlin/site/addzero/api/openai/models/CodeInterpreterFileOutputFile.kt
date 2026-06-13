// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CodeInterpreterFileOutputFile(
    /**
     * The MIME type of the file.
     */
    @SerialName("mime_type")
    val mimeType: String,
    /**
     * The ID of the file.
     */
    @SerialName("file_id")
    val fileId: String
)
