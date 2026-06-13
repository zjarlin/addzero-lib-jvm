// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A chat message that makes up the prompt or context. May include variable references to the `item`
 * namespace, ie {{item.name}}.
 */
@Serializable
data class CreateEvalItem(
    val value: Map<String, JsonElement> = emptyMap()
)
