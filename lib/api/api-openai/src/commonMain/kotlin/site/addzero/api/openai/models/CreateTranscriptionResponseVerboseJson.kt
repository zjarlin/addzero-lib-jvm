// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Represents a verbose json transcription response returned by model, based on the provided input.
 */
@Serializable
data class CreateTranscriptionResponseVerboseJson(
    /**
     * The language of the input audio.
     */
    val language: String,
    /**
     * The duration of the input audio.
     */
    val duration: Double,
    /**
     * The transcribed text.
     */
    val text: String,
    /**
     * Extracted words and their corresponding timestamps.
     */
    val words: List<site.addzero.api.openai.models.TranscriptionWord>? = null,
    /**
     * Segments of the transcribed text and their corresponding details.
     */
    val segments: List<site.addzero.api.openai.models.TranscriptionSegment>? = null,
    val usage: site.addzero.api.openai.models.TranscriptTextUsageDuration? = null
)
