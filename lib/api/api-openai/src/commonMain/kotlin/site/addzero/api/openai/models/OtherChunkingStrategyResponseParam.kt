// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * This is returned when the chunking strategy is unknown. Typically, this is because the file was
 * indexed before the `chunking_strategy` concept was introduced in the API.
 */
@Serializable
data class OtherChunkingStrategyResponseParam(
    /**
     * Always `other`.
     */
    val type: String
)
