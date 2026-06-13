// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An audio output from the model.
 */
@Serializable
data class OutputAudio(
    /**
     * The type of the output audio. Always `output_audio`.
     */
    val type: String,
    /**
     * Base64-encoded audio data from the model.
     */
    val data: String,
    /**
     * The transcript of the audio data from the model.
     */
    val transcript: String
)
