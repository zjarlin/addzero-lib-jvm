// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the code interpreter is actively interpreting the code snippet.
 */
@Serializable
data class ResponseCodeInterpreterCallInterpretingEvent(
    /**
     * The type of the event. Always `response.code_interpreter_call.interpreting`.
     */
    val type: String,
    /**
     * The index of the output item in the response for which the code interpreter is interpreting code.
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
