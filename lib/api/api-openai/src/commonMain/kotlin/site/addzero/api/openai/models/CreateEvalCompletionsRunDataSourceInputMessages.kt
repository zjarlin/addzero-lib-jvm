// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * TemplateInputMessages
 */
@Serializable
data class CreateEvalCompletionsRunDataSourceInputMessages(
    /**
     * The type of input messages. Always `template`.
     */
    val type: String,
    /**
     * A list of chat messages forming the prompt or context. May include variable references to the `item`
     * namespace, ie {{item.name}}.
     */
    val template: List<JsonElement>
)
