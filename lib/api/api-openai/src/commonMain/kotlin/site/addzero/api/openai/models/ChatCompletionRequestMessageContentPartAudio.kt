// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Learn about [audio inputs](/docs/guides/audio).
 */
@Serializable
data class ChatCompletionRequestMessageContentPartAudio(
    /**
     * The type of the content part. Always `input_audio`.
     */
    val type: String,
    @SerialName("input_audio")
    val inputAudio: site.addzero.api.openai.models.ChatCompletionRequestMessageContentPartAudioInputAudio
)
