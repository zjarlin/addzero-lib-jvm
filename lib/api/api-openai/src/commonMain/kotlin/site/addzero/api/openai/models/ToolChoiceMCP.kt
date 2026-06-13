// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Use this option to force the model to call a specific tool on a remote MCP server.
 */
@Serializable
data class ToolChoiceMCP(
    /**
     * For MCP tools, the type is always `mcp`.
     */
    val type: String,
    /**
     * The label of the MCP server to use.
     */
    @SerialName("server_label")
    val serverLabel: String,
    val name: String? = null
)
