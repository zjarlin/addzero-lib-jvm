// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when translated transcript text is available. Transcript deltas are append-only text
 * fragments. Clients should not insert unconditional spaces between deltas.
 */
@Serializable
data class RealtimeTranslationServerEventSessionOutputTranscriptDelta(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `session.output_transcript.delta`.
     */
    val type: String,
    /**
     * Append-only transcript text for the translated output audio.
     */
    val delta: String,
    @SerialName("elapsed_ms")
    val elapsedMs: Int? = null
)
