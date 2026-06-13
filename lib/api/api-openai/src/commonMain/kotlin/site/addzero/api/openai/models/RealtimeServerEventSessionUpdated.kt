// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Returned when a session is updated with a `session.update` event, unless there is an error.
 */
@Serializable
data class RealtimeServerEventSessionUpdated(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `session.updated`.
     */
    val type: String,
    /**
     * The session configuration.
     */
    val session: JsonElement
)
