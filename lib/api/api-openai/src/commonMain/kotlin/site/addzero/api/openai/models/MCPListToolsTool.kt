// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A tool available on an MCP server.
 */
@Serializable
data class MCPListToolsTool(
    /**
     * The name of the tool.
     */
    val name: String,
    val description: String? = null,
    /**
     * The JSON schema describing the tool's input.
     */
    @SerialName("input_schema")
    val inputSchema: Map<String, JsonElement>,
    val annotations: Map<String, JsonElement>? = null
)
