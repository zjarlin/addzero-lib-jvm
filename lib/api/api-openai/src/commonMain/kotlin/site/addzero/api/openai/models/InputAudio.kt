// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An audio input to the model.
 */
@Serializable
data class InputAudio(
    /**
     * The type of the input item. Always `input_audio`.
     */
    val type: String,
    @SerialName("input_audio")
    val inputAudio: site.addzero.api.openai.models.InputAudioInputAudio
)
