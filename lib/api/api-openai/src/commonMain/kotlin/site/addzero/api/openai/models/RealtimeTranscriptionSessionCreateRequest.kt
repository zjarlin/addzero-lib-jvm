// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Realtime transcription session object configuration.
 */
@Serializable
data class RealtimeTranscriptionSessionCreateRequest(
    /**
     * Configuration for turn detection. Can be set to `null` to turn off. Server VAD means that the model
     * will detect the start and end of speech based on audio volume and respond at the end of user speech.
     */
    @SerialName("turn_detection")
    val turnDetection: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateRequestTurnDetection? = null,
    /**
     * Configuration for input audio noise reduction. This can be set to `null` to turn off. Noise
     * reduction filters audio added to the input audio buffer before it is sent to VAD and the model.
     * Filtering the audio can improve VAD and turn detection accuracy (reducing false positives) and model
     * performance by improving perception of the input audio.
     */
    @SerialName("input_audio_noise_reduction")
    val inputAudioNoiseReduction: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateRequestInputAudioNoiseReduction? = null,
    /**
     * The format of input audio. Options are `pcm16`, `g711_ulaw`, or `g711_alaw`. For `pcm16`, input
     * audio must be 16-bit PCM at a 24kHz sample rate, single channel (mono), and little-endian byte
     * order.
     */
    @SerialName("input_audio_format")
    val inputAudioFormat: String? = "pcm16",
    /**
     * Configuration for input audio transcription. The client can optionally set the language and prompt
     * for transcription, these offer additional guidance to the transcription service.
     */
    @SerialName("input_audio_transcription")
    val inputAudioTranscription: site.addzero.api.openai.models.AudioTranscription? = null,
    /**
     * The set of items to include in the transcription. Current available items are:
     * `item.input_audio_transcription.logprobs`
     */
    val include: List<String>? = null
)
