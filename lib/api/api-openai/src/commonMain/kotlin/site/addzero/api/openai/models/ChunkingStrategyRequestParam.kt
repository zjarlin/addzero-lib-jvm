// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The chunking strategy used to chunk the file(s). If not set, will use the `auto` strategy.
 */
@Serializable
data class ChunkingStrategyRequestParam(
    val value: Map<String, JsonElement> = emptyMap()
)
