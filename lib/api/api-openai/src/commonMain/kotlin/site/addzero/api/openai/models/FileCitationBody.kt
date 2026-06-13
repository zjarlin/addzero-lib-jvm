// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A citation to a file.
 */
@Serializable
data class FileCitationBody(
    /**
     * The type of the file citation. Always `file_citation`.
     */
    val type: String = "file_citation",
    /**
     * The ID of the file.
     */
    @SerialName("file_id")
    val fileId: String,
    /**
     * The index of the file in the list of files.
     */
    val index: Int,
    /**
     * The filename of the file cited.
     */
    val filename: String
)
