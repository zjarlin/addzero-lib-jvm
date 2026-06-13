// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeTranscriptionSessionCreateRequestGAAudioInput(
    val format: site.addzero.api.openai.models.RealtimeAudioFormats? = null,
    /**
     * Configuration for input audio transcription, defaults to off and can be set to `null` to turn off
     * once on. Input audio transcription is not native to the model, since the model consumes audio
     * directly. Transcription runs asynchronously through [the /audio/transcriptions endpoint](/docs/api-
     * reference/audio/createTranscription) and should be treated as guidance of input audio content rather
     * than precisely what the model heard. The client can optionally set the language and prompt for
     * transcription, these offer additional guidance to the transcription service.
     */
    val transcription: site.addzero.api.openai.models.AudioTranscription? = null,
    /**
     * Configuration for input audio noise reduction. This can be set to `null` to turn off. Noise
     * reduction filters audio added to the input audio buffer before it is sent to VAD and the model.
     * Filtering the audio can improve VAD and turn detection accuracy (reducing false positives) and model
     * performance by improving perception of the input audio.
     */
    @SerialName("noise_reduction")
    val noiseReduction: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateRequestGAAudioInputNoiseReduction? = null,
    @SerialName("turn_detection")
    val turnDetection: site.addzero.api.openai.models.RealtimeTurnDetection? = null
)
