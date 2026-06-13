// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeSessionCreateResponseAudioInput(
    val format: site.addzero.api.openai.models.RealtimeAudioFormats? = null,
    /**
     * Configuration for input audio transcription.
     */
    val transcription: site.addzero.api.openai.models.AudioTranscriptionResponse? = null,
    /**
     * Configuration for input audio noise reduction.
     */
    @SerialName("noise_reduction")
    val noiseReduction: site.addzero.api.openai.models.RealtimeSessionCreateResponseAudioInputNoiseReduction? = null,
    /**
     * Configuration for turn detection.
     */
    @SerialName("turn_detection")
    val turnDetection: site.addzero.api.openai.models.RealtimeSessionCreateResponseAudioInputTurnDetection? = null
)
