// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Gracefully close the realtime translation session. The server flushes pending input audio and emits
 * any remaining translated output before closing the session.
 */
@Serializable
data class RealtimeTranslationClientEventSessionClose(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `session.close`.
     */
    val type: String
)
