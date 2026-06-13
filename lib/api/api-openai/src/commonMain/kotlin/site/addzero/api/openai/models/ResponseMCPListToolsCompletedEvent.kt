// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the list of available MCP tools has been successfully retrieved.
 */
@Serializable
data class ResponseMCPListToolsCompletedEvent(
    /**
     * The type of the event. Always 'response.mcp_list_tools.completed'.
     */
    val type: String,
    /**
     * The ID of the MCP tool call item that produced this output.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that was processed.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
