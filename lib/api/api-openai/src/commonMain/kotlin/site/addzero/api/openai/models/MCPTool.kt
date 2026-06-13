// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Give the model access to additional tools via remote Model Context Protocol (MCP) servers. [Learn
 * more about MCP](/docs/guides/tools-remote-mcp).
 */
@Serializable
data class MCPTool(
    /**
     * The type of the MCP tool. Always `mcp`.
     */
    val type: String,
    /**
     * A label for this MCP server, used to identify it in tool calls.
     */
    @SerialName("server_label")
    val serverLabel: String,
    /**
     * The URL for the MCP server. One of `server_url` or `connector_id` must be provided.
     */
    @SerialName("server_url")
    val serverUrl: String? = null,
    /**
     * Identifier for service connectors, like those available in ChatGPT. One of `server_url` or
     * `connector_id` must be provided. Learn more about service connectors [here](/docs/guides/tools-
     * remote-mcp#connectors). Currently supported `connector_id` values are: - Dropbox:
     * `connector_dropbox` - Gmail: `connector_gmail` - Google Calendar: `connector_googlecalendar` -
     * Google Drive: `connector_googledrive` - Microsoft Teams: `connector_microsoftteams` - Outlook
     * Calendar: `connector_outlookcalendar` - Outlook Email: `connector_outlookemail` - SharePoint:
     * `connector_sharepoint`
     */
    @SerialName("connector_id")
    val connectorId: String? = null,
    /**
     * An OAuth access token that can be used with a remote MCP server, either with a custom MCP server URL
     * or a service connector. Your application must handle the OAuth authorization flow and provide the
     * token here.
     */
    val authorization: String? = null,
    /**
     * Optional description of the MCP server, used to provide more context.
     */
    @SerialName("server_description")
    val serverDescription: String? = null,
    val headers: Map<String, String>? = null,
    @SerialName("allowed_tools")
    val allowedTools: JsonElement? = null,
    @SerialName("require_approval")
    val requireApproval: JsonElement? = null,
    /**
     * Whether this MCP tool is deferred and discovered via tool search.
     */
    @SerialName("defer_loading")
    val deferLoading: Boolean? = null
)
