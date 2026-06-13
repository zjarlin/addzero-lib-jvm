// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when an MCP tool call has started and is in progress.
 */
@Serializable
data class RealtimeBetaServerEventResponseMCPCallInProgress(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.mcp_call.in_progress`.
     */
    val type: String,
    /**
     * The index of the output item in the response.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The ID of the MCP tool call item.
     */
    @SerialName("item_id")
    val itemId: String
)
