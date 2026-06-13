// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the arguments for an MCP tool call are finalized.
 */
@Serializable
data class ResponseMCPCallArgumentsDoneEvent(
    /**
     * The type of the event. Always 'response.mcp_call_arguments.done'.
     */
    val type: String,
    /**
     * The index of the output item in the response's output array.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The unique identifier of the MCP tool call item being processed.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * A JSON string containing the finalized arguments for the MCP tool call.
     */
    val arguments: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
