// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when there is an additional text delta. This is also the first event emitted when the
 * transcription starts. Only emitted when you [create a transcription](/docs/api-
 * reference/audio/create-transcription) with the `Stream` parameter set to `true`.
 */
@Serializable
data class TranscriptTextDeltaEvent(
    /**
     * The type of the event. Always `transcript.text.delta`.
     */
    val type: String,
    /**
     * The text delta that was additionally transcribed.
     */
    val delta: String,
    /**
     * The log probabilities of the delta. Only included if you [create a transcription](/docs/api-
     * reference/audio/create-transcription) with the `include[]` parameter set to `logprobs`.
     */
    val logprobs: List<site.addzero.api.openai.models.TranscriptTextDeltaEventLogprob>? = null,
    /**
     * Identifier of the diarized segment that this delta belongs to. Only present when using
     * `gpt-4o-transcribe-diarize`.
     */
    @SerialName("segment_id")
    val segmentId: String? = null
)
