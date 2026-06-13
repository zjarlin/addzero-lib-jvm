// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when an error occurs, which could be a client problem or a server problem. Most errors are
 * recoverable and the session will stay open, we recommend to implementors to monitor and log error
 * messages by default.
 */
@Serializable
data class RealtimeServerEventError(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `error`.
     */
    val type: String,
    /**
     * Details of the error.
     */
    val error: site.addzero.api.openai.models.RealtimeServerEventErrorError
)
