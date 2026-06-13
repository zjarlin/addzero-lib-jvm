// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a Response is done streaming. Always emitted, no matter the final state. The Response
 * object included in the `response.done` event will include all output Items in the Response but will
 * omit the raw audio data.
 */
@Serializable
data class RealtimeBetaServerEventResponseDone(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.done`.
     */
    val type: String,
    val response: site.addzero.api.openai.models.RealtimeBetaResponse
)
