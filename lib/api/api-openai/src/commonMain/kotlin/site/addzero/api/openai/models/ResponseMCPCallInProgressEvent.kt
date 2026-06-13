// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when an MCP tool call is in progress.
 */
@Serializable
data class ResponseMCPCallInProgressEvent(
    /**
     * The type of the event. Always 'response.mcp_call.in_progress'.
     */
    val type: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The index of the output item in the response's output array.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The unique identifier of the MCP tool call item being processed.
     */
    @SerialName("item_id")
    val itemId: String
)
