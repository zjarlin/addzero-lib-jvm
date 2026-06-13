// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A filter object to specify which tools are allowed.
 */
@Serializable
data class MCPToolFilter(
    /**
     * List of allowed tool names.
     */
    @SerialName("tool_names")
    val toolNames: List<String>? = null,
    /**
     * Indicates whether or not a tool modifies data or is read-only. If an MCP server is [annotated with
     * `readOnlyHint`](https://modelcontextprotocol.io/specification/2025-06-18/schema#toolannotations-
     * readonlyhint), it will match this filter.
     */
    @SerialName("read_only")
    val readOnly: Boolean? = null
)
