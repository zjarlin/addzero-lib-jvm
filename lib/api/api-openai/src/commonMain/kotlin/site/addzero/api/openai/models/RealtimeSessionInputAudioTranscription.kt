// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for input audio transcription, defaults to off and can be set to `null` to turn off
 * once on. Input audio transcription is not native to the model, since the model consumes audio
 * directly. Transcription runs asynchronously through [the /audio/transcriptions
 * endpoint](https://platform.openai.com/docs/api-reference/audio/createTranscription) and should be
 * treated as guidance of input audio content rather than precisely what the model heard. The client
 * can optionally set the language and prompt for transcription, these offer additional guidance to the
 * transcription service.
 */
@Serializable
data class RealtimeSessionInputAudioTranscription(
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
