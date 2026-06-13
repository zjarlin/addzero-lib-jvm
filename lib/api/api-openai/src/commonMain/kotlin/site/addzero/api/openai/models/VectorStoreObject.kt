// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A vector store is a collection of processed files can be used by the `file_search` tool.
 */
@Serializable
data class VectorStoreObject(
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `vector_store`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The Unix timestamp (in seconds) for when the vector store was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The name of the vector store.
     */
    val name: String,
    /**
     * The total number of bytes used by the files in the vector store.
     */
    @SerialName("usage_bytes")
    val usageBytes: Int,
    @SerialName("file_counts")
    val fileCounts: site.addzero.api.openai.models.VectorStoreObjectFileCounts,
    /**
     * The status of the vector store, which can be either `expired`, `in_progress`, or `completed`. A
     * status of `completed` indicates that the vector store is ready for use.
     */
    val status: String,
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.VectorStoreExpirationAfter? = null,
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    @SerialName("last_active_at")
    val lastActiveAt: Long?,
    val metadata: site.addzero.api.openai.models.Metadata?
)
