// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StaticChunkingStrategy(
    /**
     * The maximum number of tokens in each chunk. The default value is `800`. The minimum value is `100`
     * and the maximum value is `4096`.
     */
    @SerialName("max_chunk_size_tokens")
    val maxChunkSizeTokens: Int,
    /**
     * The number of tokens that overlap between chunks. The default value is `400`. Note that the overlap
     * must not exceed half of `max_chunk_size_tokens`.
     */
    @SerialName("chunk_overlap_tokens")
    val chunkOverlapTokens: Int
)
