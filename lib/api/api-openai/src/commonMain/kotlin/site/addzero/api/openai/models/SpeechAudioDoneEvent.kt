// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Emitted when the speech synthesis is complete and all audio has been streamed.
 */
@Serializable
data class SpeechAudioDoneEvent(
    /**
     * The type of the event. Always `speech.audio.done`.
     */
    val type: String,
    /**
     * Token usage statistics for the request.
     */
    val usage: site.addzero.api.openai.models.SpeechAudioDoneEventUsage
)
