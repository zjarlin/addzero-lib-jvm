// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A list of files attached to a vector store.
 */
@Serializable
data class VectorStoreFileObject(
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `vector_store.file`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The total vector store usage in bytes. Note that this may be different from the original file size.
     */
    @SerialName("usage_bytes")
    val usageBytes: Int,
    /**
     * The Unix timestamp (in seconds) for when the vector store file was created.
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
     * The status of the vector store file, which can be either `in_progress`, `completed`, `cancelled`, or
     * `failed`. The status `completed` indicates that the vector store file is ready for use.
     */
    val status: String,
    @SerialName("last_error")
    val lastError: site.addzero.api.openai.models.VectorStoreFileObjectLastError?,
    /**
     * The strategy used to chunk the file.
     */
    @SerialName("chunking_strategy")
    val chunkingStrategy: JsonElement? = null,
    val attributes: site.addzero.api.openai.models.VectorStoreFileAttributes? = null
)
