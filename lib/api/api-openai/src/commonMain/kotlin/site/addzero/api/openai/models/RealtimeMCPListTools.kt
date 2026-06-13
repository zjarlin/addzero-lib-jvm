// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Realtime item listing tools available on an MCP server.
 */
@Serializable
data class RealtimeMCPListTools(
    /**
     * The type of the item. Always `mcp_list_tools`.
     */
    val type: String,
    /**
     * The unique ID of the list.
     */
    val id: String? = null,
    /**
     * The label of the MCP server.
     */
    @SerialName("server_label")
    val serverLabel: String,
    /**
     * The tools available on the server.
     */
    val tools: List<site.addzero.api.openai.models.MCPListToolsTool>
)
