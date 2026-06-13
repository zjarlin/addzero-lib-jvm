// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when an MCP tool call has completed successfully.
 */
@Serializable
data class ResponseMCPCallCompletedEvent(
    /**
     * The type of the event. Always 'response.mcp_call.completed'.
     */
    val type: String,
    /**
     * The ID of the MCP tool call item that completed.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that completed.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
