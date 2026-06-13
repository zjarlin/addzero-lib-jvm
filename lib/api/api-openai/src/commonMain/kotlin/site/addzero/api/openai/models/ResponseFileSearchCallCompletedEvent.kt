// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a file search call is completed (results found).
 */
@Serializable
data class ResponseFileSearchCallCompletedEvent(
    /**
     * The type of the event. Always `response.file_search_call.completed`.
     */
    val type: String,
    /**
     * The index of the output item that the file search call is initiated.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The ID of the output item that the file search call is initiated.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
