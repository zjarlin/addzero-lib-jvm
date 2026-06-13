// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class InputAudioInputAudio(
    /**
     * Base64-encoded audio data.
     */
    val data: String,
    /**
     * The format of the audio data. Currently supported formats are `mp3` and `wav`.
     */
    val format: String
)
