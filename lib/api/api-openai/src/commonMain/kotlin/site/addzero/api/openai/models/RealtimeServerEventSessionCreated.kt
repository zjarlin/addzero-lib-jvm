// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Returned when a Session is created. Emitted automatically when a new connection is established as
 * the first server event. This event will contain the default Session configuration.
 */
@Serializable
data class RealtimeServerEventSessionCreated(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `session.created`.
     */
    val type: String,
    /**
     * The session configuration.
     */
    val session: JsonElement
)
