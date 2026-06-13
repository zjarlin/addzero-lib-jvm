// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when there is a partial function-call arguments delta.
 */
@Serializable
data class ResponseFunctionCallArgumentsDeltaEvent(
    /**
     * The type of the event. Always `response.function_call_arguments.delta`.
     */
    val type: String,
    /**
     * The ID of the output item that the function-call arguments delta is added to.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that the function-call arguments delta is added to.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The function-call arguments delta that is added.
     */
    val delta: String
)
