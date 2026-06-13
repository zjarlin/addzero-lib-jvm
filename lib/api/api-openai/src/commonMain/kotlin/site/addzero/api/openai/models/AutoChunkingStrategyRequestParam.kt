// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The default strategy. This strategy currently uses a `max_chunk_size_tokens` of `800` and
 * `chunk_overlap_tokens` of `400`.
 */
@Serializable
data class AutoChunkingStrategyRequestParam(
    /**
     * Always `auto`.
     */
    val type: String
)
