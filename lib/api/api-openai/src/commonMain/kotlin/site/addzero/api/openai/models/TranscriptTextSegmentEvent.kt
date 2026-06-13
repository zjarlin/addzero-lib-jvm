// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Emitted when a diarized transcription returns a completed segment with speaker information. Only
 * emitted when you [create a transcription](/docs/api-reference/audio/create-transcription) with
 * `stream` set to `true` and `response_format` set to `diarized_json`.
 */
@Serializable
data class TranscriptTextSegmentEvent(
    /**
     * The type of the event. Always `transcript.text.segment`.
     */
    val type: String,
    /**
     * Unique identifier for the segment.
     */
    val id: String,
    /**
     * Start timestamp of the segment in seconds.
     */
    val start: Double,
    /**
     * End timestamp of the segment in seconds.
     */
    val end: Double,
    /**
     * Transcript text for this segment.
     */
    val text: String,
    /**
     * Speaker label for this segment.
     */
    val speaker: String
)
