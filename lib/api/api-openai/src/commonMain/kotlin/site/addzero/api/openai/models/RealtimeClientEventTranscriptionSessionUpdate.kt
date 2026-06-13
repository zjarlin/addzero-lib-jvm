// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Send this event to update a transcription session.
 */
@Serializable
data class RealtimeClientEventTranscriptionSessionUpdate(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `transcription_session.update`.
     */
    val type: String,
    val session: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateRequest
)
