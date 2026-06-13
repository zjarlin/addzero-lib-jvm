// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when translated output audio is available. Output audio deltas are 200 ms frames of PCM16
 * audio.
 */
@Serializable
data class RealtimeTranslationServerEventSessionOutputAudioDelta(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `session.output_audio.delta`.
     */
    val type: String,
    /**
     * Base64-encoded translated audio data.
     */
    val delta: String,
    /**
     * Sample rate of the audio delta.
     */
    @SerialName("sample_rate")
    val sampleRate: Int? = 24000,
    /**
     * Number of audio channels.
     */
    val channels: Int? = 1,
    /**
     * Audio encoding for `delta`.
     */
    val format: String? = null,
    @SerialName("elapsed_ms")
    val elapsedMs: Int? = null
)
