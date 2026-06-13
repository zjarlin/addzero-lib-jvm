// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Static Chunking Strategy
 */
@Serializable
data class StaticChunkingStrategyResponseParam(
    /**
     * Always `static`.
     */
    val type: String,
    val static: site.addzero.api.openai.models.StaticChunkingStrategy
)
