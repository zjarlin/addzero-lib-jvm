// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a file search is currently searching.
 */
@Serializable
data class ResponseFileSearchCallSearchingEvent(
    /**
     * The type of the event. Always `response.file_search_call.searching`.
     */
    val type: String,
    /**
     * The index of the output item that the file search call is searching.
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
