// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when an error occurs.
 */
@Serializable
data class ResponseErrorEvent(
    /**
     * The type of the event. Always `error`.
     */
    val type: String,
    val code: String?,
    /**
     * The error message.
     */
    val message: String,
    val param: String?,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
