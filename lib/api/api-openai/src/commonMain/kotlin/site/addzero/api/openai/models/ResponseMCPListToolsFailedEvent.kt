// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the attempt to list available MCP tools has failed.
 */
@Serializable
data class ResponseMCPListToolsFailedEvent(
    /**
     * The type of the event. Always 'response.mcp_list_tools.failed'.
     */
    val type: String,
    /**
     * The ID of the MCP tool call item that failed.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that failed.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
