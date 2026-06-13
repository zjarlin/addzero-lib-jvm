// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Constrains the tools available to the model to a pre-defined set.
 */
@Serializable
data class ToolChoiceAllowed(
    /**
     * Allowed tool configuration type. Always `allowed_tools`.
     */
    val type: String,
    /**
     * Constrains the tools available to the model to a pre-defined set. `auto` allows the model to pick
     * from among the allowed tools and generate a message. `required` requires the model to call one or
     * more of the allowed tools.
     */
    val mode: String,
    /**
     * A list of tool definitions that the model should be allowed to call. For the Responses API, the list
     * of tool definitions might look like: ```json [ { "type": "function", "name": "get_weather" }, {
     * "type": "mcp", "server_label": "deepwiki" }, { "type": "image_generation" } ] ```
     */
    val tools: List<Map<String, JsonElement>>
)
