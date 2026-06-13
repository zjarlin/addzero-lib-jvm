// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VectorStoreSearchResultItem(
    /**
     * The ID of the vector store file.
     */
    @SerialName("file_id")
    val fileId: String,
    /**
     * The name of the vector store file.
     */
    val filename: String,
    /**
     * The similarity score for the result.
     */
    val score: Double,
    val attributes: site.addzero.api.openai.models.VectorStoreFileAttributes?,
    /**
     * Content chunks from the file.
     */
    val content: List<site.addzero.api.openai.models.VectorStoreSearchResultContentObject>
)
