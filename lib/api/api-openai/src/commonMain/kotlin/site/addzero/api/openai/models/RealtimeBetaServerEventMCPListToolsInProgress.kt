// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when listing MCP tools is in progress for an item.
 */
@Serializable
data class RealtimeBetaServerEventMCPListToolsInProgress(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `mcp_list_tools.in_progress`.
     */
    val type: String,
    /**
     * The ID of the MCP list tools item.
     */
    @SerialName("item_id")
    val itemId: String
)
