// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateVectorStoreFileBatchRequest(
    /**
     * A list of [File](/docs/api-reference/files) IDs that the vector store should use. Useful for tools
     * like `file_search` that can access files. If `attributes` or `chunking_strategy` are provided, they
     * will be applied to all files in the batch. The maximum batch size is 2000 files. This endpoint is
     * recommended for multi-file ingestion and helps reduce per-vector-store write request pressure.
     * Mutually exclusive with `files`.
     */
    @SerialName("file_ids")
    val fileIds: List<String>? = null,
    /**
     * A list of objects that each include a `file_id` plus optional `attributes` or `chunking_strategy`.
     * Use this when you need to override metadata for specific files. The global `attributes` or
     * `chunking_strategy` will be ignored and must be specified for each file. The maximum batch size is
     * 2000 files. This endpoint is recommended for multi-file ingestion and helps reduce per-vector-store
     * write request pressure. Mutually exclusive with `file_ids`.
     */
    val files: List<site.addzero.api.openai.models.CreateVectorStoreFileRequest>? = null,
    @SerialName("chunking_strategy")
    val chunkingStrategy: site.addzero.api.openai.models.ChunkingStrategyRequestParam? = null,
    val attributes: site.addzero.api.openai.models.VectorStoreFileAttributes? = null
)
