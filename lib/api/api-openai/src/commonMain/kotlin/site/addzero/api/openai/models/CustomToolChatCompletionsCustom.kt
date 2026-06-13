// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Properties of the custom tool.
 */
@Serializable
data class CustomToolChatCompletionsCustom(
    /**
     * The name of the custom tool, used to identify it in tool calls.
     */
    val name: String,
    /**
     * Optional description of the custom tool, used to provide more context.
     */
    val description: String? = null,
    /**
     * The input format for the custom tool. Default is unconstrained text.
     */
    val format: JsonElement? = null
)
