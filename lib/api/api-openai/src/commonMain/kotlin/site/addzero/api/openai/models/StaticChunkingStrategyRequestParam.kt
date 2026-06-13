// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Customize your own chunking strategy by setting chunk size and chunk overlap.
 */
@Serializable
data class StaticChunkingStrategyRequestParam(
    /**
     * Always `static`.
     */
    val type: String,
    val static: site.addzero.api.openai.models.StaticChunkingStrategy
)
