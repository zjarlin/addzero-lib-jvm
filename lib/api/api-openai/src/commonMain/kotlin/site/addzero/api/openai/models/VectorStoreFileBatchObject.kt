// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A batch of files attached to a vector store.
 */
@Serializable
data class VectorStoreFileBatchObject(
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `vector_store.file_batch`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The Unix timestamp (in seconds) for when the vector store files batch was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The ID of the [vector store](/docs/api-reference/vector-stores/object) that the [File](/docs/api-
     * reference/files) is attached to.
     */
    @SerialName("vector_store_id")
    val vectorStoreId: String,
    /**
     * The status of the vector store files batch, which can be either `in_progress`, `completed`,
     * `cancelled` or `failed`.
     */
    val status: String,
    @SerialName("file_counts")
    val fileCounts: site.addzero.api.openai.models.VectorStoreFileBatchObjectFileCounts
)
