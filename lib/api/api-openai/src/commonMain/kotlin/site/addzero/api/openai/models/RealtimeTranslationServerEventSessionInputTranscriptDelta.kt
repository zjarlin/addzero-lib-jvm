// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when optional source-language transcript text is available. This event is emitted only when
 * `audio.input.transcription` is configured. Transcript deltas are append-only text fragments. Clients
 * should not insert unconditional spaces between deltas.
 */
@Serializable
data class RealtimeTranslationServerEventSessionInputTranscriptDelta(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `session.input_transcript.delta`.
     */
    val type: String,
    /**
     * Append-only source-language transcript text.
     */
    val delta: String,
    @SerialName("elapsed_ms")
    val elapsedMs: Int? = null
)
