// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Upload permissions and limits applied to the session.
 */
@Serializable
data class ChatSessionFileUpload(
    /**
     * Indicates if uploads are enabled for the session.
     */
    val enabled: Boolean,
    @SerialName("max_file_size")
    val maxFileSize: Int?,
    @SerialName("max_files")
    val maxFiles: Int?
)
