// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the system is in the process of retrieving the list of available MCP tools.
 */
@Serializable
data class ResponseMCPListToolsInProgressEvent(
    /**
     * The type of the event. Always 'response.mcp_list_tools.in_progress'.
     */
    val type: String,
    /**
     * The ID of the MCP tool call item that is being processed.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that is being processed.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
