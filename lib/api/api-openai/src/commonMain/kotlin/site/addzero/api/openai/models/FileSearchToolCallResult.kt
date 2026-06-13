// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FileSearchToolCallResult(
    /**
     * The unique ID of the file.
     */
    @SerialName("file_id")
    val fileId: String? = null,
    /**
     * The text that was retrieved from the file.
     */
    val text: String? = null,
    /**
     * The name of the file.
     */
    val filename: String? = null,
    val attributes: site.addzero.api.openai.models.VectorStoreFileAttributes? = null,
    /**
     * The relevance score of the file - a value between 0 and 1.
     */
    val score: Float? = null
)
