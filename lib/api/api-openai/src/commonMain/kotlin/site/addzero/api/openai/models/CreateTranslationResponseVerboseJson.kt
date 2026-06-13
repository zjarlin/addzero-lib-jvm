// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateTranslationResponseVerboseJson(
    /**
     * The language of the output translation (always `english`).
     */
    val language: String,
    /**
     * The duration of the input audio.
     */
    val duration: Double,
    /**
     * The translated text.
     */
    val text: String,
    /**
     * Segments of the translated text and their corresponding details.
     */
    val segments: List<site.addzero.api.openai.models.TranscriptionSegment>? = null
)
