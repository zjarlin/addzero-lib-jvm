// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a translation session is created. Emitted automatically when a new connection is
 * established as the first server event. This event contains the default translation session
 * configuration.
 */
@Serializable
data class RealtimeTranslationServerEventSessionCreated(
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
     * The translation session configuration.
     */
    val session: site.addzero.api.openai.models.RealtimeTranslationSession
)
