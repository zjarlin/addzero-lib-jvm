// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A request for human approval of a tool invocation.
 */
@Serializable
data class MCPApprovalRequest(
    /**
     * The type of the item. Always `mcp_approval_request`.
     */
    val type: String,
    /**
     * The unique ID of the approval request.
     */
    val id: String,
    /**
     * The label of the MCP server making the request.
     */
    @SerialName("server_label")
    val serverLabel: String,
    /**
     * The name of the tool to run.
     */
    val name: String,
    /**
     * A JSON string of arguments for the tool.
     */
    val arguments: String
)
