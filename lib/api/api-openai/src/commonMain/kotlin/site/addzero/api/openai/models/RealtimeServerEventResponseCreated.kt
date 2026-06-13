// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a new Response is created. The first event of response creation, where the response is
 * in an initial state of `in_progress`.
 */
@Serializable
data class RealtimeServerEventResponseCreated(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.created`.
     */
    val type: String,
    val response: site.addzero.api.openai.models.RealtimeResponse
)
