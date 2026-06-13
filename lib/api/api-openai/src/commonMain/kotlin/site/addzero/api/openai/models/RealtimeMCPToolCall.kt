// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A Realtime item representing an invocation of a tool on an MCP server.
 */
@Serializable
data class RealtimeMCPToolCall(
    /**
     * The type of the item. Always `mcp_call`.
     */
    val type: String,
    /**
     * The unique ID of the tool call.
     */
    val id: String,
    /**
     * The label of the MCP server running the tool.
     */
    @SerialName("server_label")
    val serverLabel: String,
    /**
     * The name of the tool that was run.
     */
    val name: String,
    /**
     * A JSON string of the arguments passed to the tool.
     */
    val arguments: String,
    @SerialName("approval_request_id")
    val approvalRequestId: String? = null,
    val output: String? = null,
    val error: JsonElement? = null
)
