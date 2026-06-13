// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a translation session is updated with a `session.update` event, unless there is an
 * error.
 */
@Serializable
data class RealtimeTranslationServerEventSessionUpdated(
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
     * The translation session configuration.
     */
    val session: site.addzero.api.openai.models.RealtimeTranslationSession
)
