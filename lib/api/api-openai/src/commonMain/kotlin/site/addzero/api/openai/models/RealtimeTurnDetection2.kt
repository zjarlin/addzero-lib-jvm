// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Server-side voice activity detection (VAD) which flips on when user speech is detected and off after
 * a period of silence.
 */
@Serializable
data class RealtimeTurnDetection2(
    /**
     * Type of turn detection, `server_vad` to turn on simple Server VAD.
     */
    val type: String = "server_vad",
    /**
     * Used only for `server_vad` mode. Activation threshold for VAD (0.0 to 1.0), this defaults to 0.5. A
     * higher threshold will require louder audio to activate the model, and thus might perform better in
     * noisy environments.
     */
    val threshold: Double? = null,
    /**
     * Used only for `server_vad` mode. Amount of audio to include before the VAD detected speech (in
     * milliseconds). Defaults to 300ms.
     */
    @SerialName("prefix_padding_ms")
    val prefixPaddingMs: Int? = null,
    /**
     * Used only for `server_vad` mode. Duration of silence to detect speech stop (in milliseconds).
     * Defaults to 500ms. With shorter values the model will respond more quickly, but may jump in on short
     * pauses from the user.
     */
    @SerialName("silence_duration_ms")
    val silenceDurationMs: Int? = null,
    /**
     * Whether or not to automatically generate a response when a VAD stop event occurs. If
     * `interrupt_response` is set to `false` this may fail to create a response if the model is already
     * responding. If both `create_response` and `interrupt_response` are set to `false`, the model will
     * never respond automatically but VAD events will still be emitted.
     */
    @SerialName("create_response")
    val createResponse: Boolean? = true,
    /**
     * Whether or not to automatically interrupt (cancel) any ongoing response with output to the default
     * conversation (i.e. `conversation` of `auto`) when a VAD start event occurs. If `true` then the
     * response will be cancelled, otherwise it will continue until complete. If both `create_response` and
     * `interrupt_response` are set to `false`, the model will never respond automatically but VAD events
     * will still be emitted.
     */
    @SerialName("interrupt_response")
    val interruptResponse: Boolean? = true,
    @SerialName("idle_timeout_ms")
    val idleTimeoutMs: Int? = null
)
