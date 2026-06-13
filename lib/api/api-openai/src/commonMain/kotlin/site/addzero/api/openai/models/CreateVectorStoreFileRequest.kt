// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateVectorStoreFileRequest(
    /**
     * A [File](/docs/api-reference/files) ID that the vector store should use. Useful for tools like
     * `file_search` that can access files. For multi-file ingestion, we recommend
     * [`file_batches`](/docs/api-reference/vector-stores-file-batches/createBatch) to minimize per-vector-
     * store write requests.
     */
    @SerialName("file_id")
    val fileId: String,
    @SerialName("chunking_strategy")
    val chunkingStrategy: site.addzero.api.openai.models.ChunkingStrategyRequestParam? = null,
    val attributes: site.addzero.api.openai.models.VectorStoreFileAttributes? = null
)
