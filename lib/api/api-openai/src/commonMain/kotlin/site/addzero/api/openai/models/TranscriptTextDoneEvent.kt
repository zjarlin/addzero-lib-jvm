// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Emitted when the transcription is complete. Contains the complete transcription text. Only emitted
 * when you [create a transcription](/docs/api-reference/audio/create-transcription) with the `Stream`
 * parameter set to `true`.
 */
@Serializable
data class TranscriptTextDoneEvent(
    /**
     * The type of the event. Always `transcript.text.done`.
     */
    val type: String,
    /**
     * The text that was transcribed.
     */
    val text: String,
    /**
     * The log probabilities of the individual tokens in the transcription. Only included if you [create a
     * transcription](/docs/api-reference/audio/create-transcription) with the `include[]` parameter set to
     * `logprobs`.
     */
    val logprobs: List<site.addzero.api.openai.models.TranscriptTextDoneEventLogprob>? = null,
    val usage: site.addzero.api.openai.models.TranscriptTextUsageTokens? = null
)
