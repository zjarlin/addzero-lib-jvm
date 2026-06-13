// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeTranscriptionSessionCreateResponseGAAudioInput(
    val format: site.addzero.api.openai.models.RealtimeAudioFormats? = null,
    /**
     * Configuration of the transcription model.
     */
    val transcription: site.addzero.api.openai.models.AudioTranscriptionResponse? = null,
    /**
     * Configuration for input audio noise reduction.
     */
    @SerialName("noise_reduction")
    val noiseReduction: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateResponseGAAudioInputNoiseReduction? = null,
    /**
     * Configuration for turn detection. For `gpt-realtime-whisper`, this must be `null`; VAD is not
     * supported.
     */
    @SerialName("turn_detection")
    val turnDetection: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateResponseGAAudioInputTurnDetection? = null
)
