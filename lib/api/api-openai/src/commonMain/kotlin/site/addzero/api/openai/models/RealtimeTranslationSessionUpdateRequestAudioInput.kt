// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeTranslationSessionUpdateRequestAudioInput(
    val transcription: site.addzero.api.openai.models.RealtimeTranslationSessionUpdateRequestAudioInputTranscription? = null,
    @SerialName("noise_reduction")
    val noiseReduction: site.addzero.api.openai.models.RealtimeTranslationSessionUpdateRequestAudioInputNoiseReduction? = null
)
