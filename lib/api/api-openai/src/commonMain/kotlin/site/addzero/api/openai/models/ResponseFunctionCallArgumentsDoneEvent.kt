// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when function-call arguments are finalized.
 */
@Serializable
data class ResponseFunctionCallArgumentsDoneEvent(
    val type: String,
    /**
     * The ID of the item.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The name of the function that was called.
     */
    val name: String,
    /**
     * The index of the output item.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The function-call arguments.
     */
    val arguments: String
)
