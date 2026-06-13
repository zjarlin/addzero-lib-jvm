// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when a transcription session is updated with a `transcription_session.update` event, unless
 * there is an error.
 */
@Serializable
data class RealtimeServerEventTranscriptionSessionUpdated(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `transcription_session.updated`.
     */
    val type: String,
    val session: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateResponse
)
