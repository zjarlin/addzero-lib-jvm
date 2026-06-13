// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A new Realtime transcription session configuration. When a session is created on the server via REST
 * API, the session object also contains an ephemeral key. Default TTL for keys is 10 minutes. This
 * property is not present when a session is updated via the WebSocket API.
 */
@Serializable
data class RealtimeTranscriptionSessionCreateResponse(
    /**
     * Ephemeral key returned by the API. Only present when the session is created on the server via REST
     * API.
     */
    @SerialName("client_secret")
    val clientSecret: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateResponseClientSecret,
    /**
     * The set of modalities the model can respond with. To disable audio, set this to ["text"].
     */
    val modalities: JsonElement? = null,
    /**
     * The format of input audio. Options are `pcm16`, `g711_ulaw`, or `g711_alaw`.
     */
    @SerialName("input_audio_format")
    val inputAudioFormat: String? = null,
    /**
     * Configuration of the transcription model.
     */
    @SerialName("input_audio_transcription")
    val inputAudioTranscription: site.addzero.api.openai.models.AudioTranscriptionResponse? = null,
    /**
     * Configuration for turn detection. Can be set to `null` to turn off. Server VAD means that the model
     * will detect the start and end of speech based on audio volume and respond at the end of user speech.
     */
    @SerialName("turn_detection")
    val turnDetection: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateResponseTurnDetection? = null
)
