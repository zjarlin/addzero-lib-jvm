// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestMessageContentPartAudioInputAudio(
    /**
     * Base64 encoded audio data.
     */
    val data: String,
    /**
     * The format of the encoded audio data. Currently supports "wav" and "mp3".
     */
    val format: String
)
