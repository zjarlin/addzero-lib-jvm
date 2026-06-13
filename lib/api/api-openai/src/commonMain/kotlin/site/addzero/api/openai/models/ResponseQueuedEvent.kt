// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a response is queued and waiting to be processed.
 */
@Serializable
data class ResponseQueuedEvent(
    /**
     * The type of the event. Always 'response.queued'.
     */
    val type: String,
    /**
     * The full response object that is queued.
     */
    val response: site.addzero.api.openai.models.Response,
    /**
     * The sequence number for this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
