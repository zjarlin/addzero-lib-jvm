// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Emitted for each chunk of audio data generated during speech synthesis.
 */
@Serializable
data class SpeechAudioDeltaEvent(
    /**
     * The type of the event. Always `speech.audio.delta`.
     */
    val type: String,
    /**
     * A chunk of Base64-encoded audio data.
     */
    val audio: String
)
