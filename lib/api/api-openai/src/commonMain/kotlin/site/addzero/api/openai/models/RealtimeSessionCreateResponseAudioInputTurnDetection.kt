// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuration for turn detection.
 */
@Serializable
data class RealtimeSessionCreateResponseAudioInputTurnDetection(
    /**
     * Type of turn detection, only `server_vad` is currently supported.
     */
    val type: String? = null,
    val threshold: Double? = null,
    @SerialName("prefix_padding_ms")
    val prefixPaddingMs: Int? = null,
    @SerialName("silence_duration_ms")
    val silenceDurationMs: Int? = null
)
