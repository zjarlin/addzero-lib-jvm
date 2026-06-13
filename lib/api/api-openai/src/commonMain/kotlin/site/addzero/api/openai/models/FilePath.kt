// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A path to a file.
 */
@Serializable
data class FilePath(
    /**
     * The type of the file path. Always `file_path`.
     */
    val type: String,
    /**
     * The ID of the file.
     */
    @SerialName("file_id")
    val fileId: String,
    /**
     * The index of the file in the list of files.
     */
    val index: Int
)
