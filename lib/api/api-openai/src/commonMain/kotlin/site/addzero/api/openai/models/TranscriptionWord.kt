// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class TranscriptionWord(
    /**
     * The text content of the word.
     */
    val word: String,
    /**
     * Start time of the word in seconds.
     */
    val start: Double,
    /**
     * End time of the word in seconds.
     */
    val end: Double
)
