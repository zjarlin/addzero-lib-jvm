// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the model response is complete.
 */
@Serializable
data class ResponseCompletedEvent(
    /**
     * The type of the event. Always `response.completed`.
     */
    val type: String,
    /**
     * Properties of the completed response.
     */
    val response: site.addzero.api.openai.models.Response,
    /**
     * The sequence number for this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
