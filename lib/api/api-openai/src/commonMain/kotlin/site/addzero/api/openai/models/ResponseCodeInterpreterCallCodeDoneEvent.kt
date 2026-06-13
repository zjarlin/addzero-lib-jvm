// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the code snippet is finalized by the code interpreter.
 */
@Serializable
data class ResponseCodeInterpreterCallCodeDoneEvent(
    /**
     * The type of the event. Always `response.code_interpreter_call_code.done`.
     */
    val type: String,
    /**
     * The index of the output item in the response for which the code is finalized.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The unique identifier of the code interpreter tool call item.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The final code snippet output by the code interpreter.
     */
    val code: String,
    /**
     * The sequence number of this event, used to order streaming events.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
