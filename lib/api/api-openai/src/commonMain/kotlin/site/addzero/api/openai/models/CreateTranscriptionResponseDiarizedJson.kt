// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a diarized transcription response returned by the model, including the combined
 * transcript and speaker-segment annotations.
 */
@Serializable
data class CreateTranscriptionResponseDiarizedJson(
    /**
     * The type of task that was run. Always `transcribe`.
     */
    val task: String,
    /**
     * Duration of the input audio in seconds.
     */
    val duration: Double,
    /**
     * The concatenated transcript text for the entire audio input.
     */
    val text: String,
    /**
     * Segments of the transcript annotated with timestamps and speaker labels.
     */
    val segments: List<site.addzero.api.openai.models.TranscriptionDiarizedSegment>,
    /**
     * Token or duration usage statistics for the request.
     */
    val usage: JsonElement? = null
)
