// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when the model-generated function call arguments are done streaming. Also emitted when a
 * Response is interrupted, incomplete, or cancelled.
 */
@Serializable
data class RealtimeServerEventResponseFunctionCallArgumentsDone(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.function_call_arguments.done`.
     */
    val type: String,
    /**
     * The ID of the response.
     */
    @SerialName("response_id")
    val responseId: String,
    /**
     * The ID of the function call item.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item in the response.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The ID of the function call.
     */
    @SerialName("call_id")
    val callId: String,
    /**
     * The name of the function that was called.
     */
    val name: String,
    /**
     * The final arguments as a JSON string.
     */
    val arguments: String
)
