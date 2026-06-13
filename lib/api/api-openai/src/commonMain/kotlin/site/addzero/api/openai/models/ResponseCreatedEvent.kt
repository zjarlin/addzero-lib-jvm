// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An event that is emitted when a response is created.
 */
@Serializable
data class ResponseCreatedEvent(
    /**
     * The type of the event. Always `response.created`.
     */
    val type: String,
    /**
     * The response that was created.
     */
    val response: site.addzero.api.openai.models.Response,
    /**
     * The sequence number for this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
