// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a code interpreter call is in progress.
 */
@Serializable
data class ResponseCodeInterpreterCallInProgressEvent(
    /**
     * The type of the event. Always `response.code_interpreter_call.in_progress`.
     */
    val type: String,
    /**
     * The index of the output item in the response for which the code interpreter call is in progress.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The unique identifier of the code interpreter tool call item.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The sequence number of this event, used to order streaming events.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
