// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a web search call is initiated.
 */
@Serializable
data class ResponseWebSearchCallInProgressEvent(
    /**
     * The type of the event. Always `response.web_search_call.in_progress`.
     */
    val type: String,
    /**
     * The index of the output item that the web search call is associated with.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * Unique ID for the output item associated with the web search call.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The sequence number of the web search call being processed.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
