// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Realtime transcription session object configuration.
 */
@Serializable
data class RealtimeTranscriptionSessionCreateRequestGA(
    /**
     * The type of session to create. Always `transcription` for transcription sessions.
     */
    val type: String,
    /**
     * Configuration for input and output audio.
     */
    val audio: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateRequestGAAudio? = null,
    /**
     * Additional fields to include in server outputs. `item.input_audio_transcription.logprobs`: Include
     * logprobs for input audio transcription.
     */
    val include: List<String>? = null
)
