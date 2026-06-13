// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A segment of diarized transcript text with speaker metadata.
 */
@Serializable
data class TranscriptionDiarizedSegment(
    /**
     * The type of the segment. Always `transcript.text.segment`.
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
     * Speaker label for this segment. When known speakers are provided, the label matches
     * `known_speaker_names[]`. Otherwise speakers are labeled sequentially using capital letters (`A`,
     * `B`, ...).
     */
    val speaker: String
)
