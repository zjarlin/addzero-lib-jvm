// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioTranscriptionResponse(
    /**
     * The model used for transcription. Current options are `whisper-1`, `gpt-4o-mini-transcribe`,
     * `gpt-4o-mini-transcribe-2025-12-15`, `gpt-4o-transcribe`, `gpt-4o-transcribe-diarize`, and `gpt-
     * realtime-whisper`.
     */
    val model: String? = null,
    /**
     * The language of the input audio.
     */
    val language: String? = null,
    /**
     * The prompt configured for input audio transcription, when present.
     */
    val prompt: String? = null
)
