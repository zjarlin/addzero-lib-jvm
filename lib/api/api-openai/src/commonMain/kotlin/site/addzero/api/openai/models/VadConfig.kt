// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VadConfig(
    /**
     * Must be set to `server_vad` to enable manual chunking using server side VAD.
     */
    val type: String,
    /**
     * Amount of audio to include before the VAD detected speech (in milliseconds).
     */
    @SerialName("prefix_padding_ms")
    val prefixPaddingMs: Int? = 300,
    /**
     * Duration of silence to detect speech stop (in milliseconds). With shorter values the model will
     * respond more quickly, but may jump in on short pauses from the user.
     */
    @SerialName("silence_duration_ms")
    val silenceDurationMs: Int? = 200,
    /**
     * Sensitivity threshold (0.0 to 1.0) for voice activity detection. A higher threshold will require
     * louder audio to activate the model, and thus might perform better in noisy environments.
     */
    val threshold: Double? = 0.5
)
