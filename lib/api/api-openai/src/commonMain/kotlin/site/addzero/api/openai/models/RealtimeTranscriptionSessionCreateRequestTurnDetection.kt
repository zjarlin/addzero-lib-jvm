// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuration for turn detection. Can be set to `null` to turn off. Server VAD means that the model
 * will detect the start and end of speech based on audio volume and respond at the end of user speech.
 */
@Serializable
data class RealtimeTranscriptionSessionCreateRequestTurnDetection(
    /**
     * Type of turn detection. Only `server_vad` is currently supported for transcription sessions.
     */
    val type: String? = null,
    /**
     * Activation threshold for VAD (0.0 to 1.0), this defaults to 0.5. A higher threshold will require
     * louder audio to activate the model, and thus might perform better in noisy environments.
     */
    val threshold: Double? = null,
    /**
     * Amount of audio to include before the VAD detected speech (in milliseconds). Defaults to 300ms.
     */
    @SerialName("prefix_padding_ms")
    val prefixPaddingMs: Int? = null,
    /**
     * Duration of silence to detect speech stop (in milliseconds). Defaults to 500ms. With shorter values
     * the model will respond more quickly, but may jump in on short pauses from the user.
     */
    @SerialName("silence_duration_ms")
    val silenceDurationMs: Int? = null
)
