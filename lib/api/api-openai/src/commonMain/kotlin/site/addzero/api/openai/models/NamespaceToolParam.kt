// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Groups function/custom tools under a shared namespace.
 */
@Serializable
data class NamespaceToolParam(
    /**
     * The type of the tool. Always `namespace`.
     */
    val type: String = "namespace",
    /**
     * The namespace name used in tool calls (for example, `crm`).
     */
    val name: String,
    /**
     * A description of the namespace shown to the model.
     */
    val description: String,
    /**
     * The function/custom tools available inside this namespace.
     */
    val tools: List<JsonElement>
)
