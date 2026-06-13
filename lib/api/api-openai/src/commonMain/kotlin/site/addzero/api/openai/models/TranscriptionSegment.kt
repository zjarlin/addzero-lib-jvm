// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TranscriptionSegment(
    /**
     * Unique identifier of the segment.
     */
    val id: Int,
    /**
     * Seek offset of the segment.
     */
    val seek: Int,
    /**
     * Start time of the segment in seconds.
     */
    val start: Double,
    /**
     * End time of the segment in seconds.
     */
    val end: Double,
    /**
     * Text content of the segment.
     */
    val text: String,
    /**
     * Array of token IDs for the text content.
     */
    val tokens: List<Int>,
    /**
     * Temperature parameter used for generating the segment.
     */
    val temperature: Float,
    /**
     * Average logprob of the segment. If the value is lower than -1, consider the logprobs failed.
     */
    @SerialName("avg_logprob")
    val avgLogprob: Float,
    /**
     * Compression ratio of the segment. If the value is greater than 2.4, consider the compression failed.
     */
    @SerialName("compression_ratio")
    val compressionRatio: Float,
    /**
     * Probability of no speech in the segment. If the value is higher than 1.0 and the `avg_logprob` is
     * below -1, consider this segment silent.
     */
    @SerialName("no_speech_prob")
    val noSpeechProb: Float
)
