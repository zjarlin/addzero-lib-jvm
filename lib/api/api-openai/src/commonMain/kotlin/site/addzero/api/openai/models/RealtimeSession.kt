// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Realtime session object for the beta interface.
 */
@Serializable
data class RealtimeSession(
    /**
     * Unique identifier for the session that looks like `sess_1234567890abcdef`.
     */
    val id: String? = null,
    /**
     * The object type. Always `realtime.session`.
     */
    @SerialName("object")
    val objectType: String? = null,
    /**
     * The set of modalities the model can respond with. To disable audio, set this to ["text"].
     */
    val modalities: JsonElement? = null,
    /**
     * The Realtime model used for this session.
     */
    val model: String? = null,
    /**
     * The default system instructions (i.e. system message) prepended to model calls. This field allows
     * the client to guide the model on desired responses. The model can be instructed on response content
     * and format, (e.g. "be extremely succinct", "act friendly", "here are examples of good responses")
     * and on audio behavior (e.g. "talk quickly", "inject emotion into your voice", "laugh frequently").
     * The instructions are not guaranteed to be followed by the model, but they provide guidance to the
     * model on the desired behavior. Note that the server sets default instructions which will be used if
     * this field is not set and are visible in the `session.created` event at the start of the session.
     */
    val instructions: String? = null,
    /**
     * The voice the model uses to respond. Voice cannot be changed during the session once the model has
     * responded with audio at least once. Current voice options are `alloy`, `ash`, `ballad`, `coral`,
     * `echo`, `sage`, `shimmer`, and `verse`.
     */
    val voice: site.addzero.api.openai.models.VoiceIdsShared? = null,
    /**
     * The format of input audio. Options are `pcm16`, `g711_ulaw`, or `g711_alaw`. For `pcm16`, input
     * audio must be 16-bit PCM at a 24kHz sample rate, single channel (mono), and little-endian byte
     * order.
     */
    @SerialName("input_audio_format")
    val inputAudioFormat: String? = "pcm16",
    /**
     * The format of output audio. Options are `pcm16`, `g711_ulaw`, or `g711_alaw`. For `pcm16`, output
     * audio is sampled at a rate of 24kHz.
     */
    @SerialName("output_audio_format")
    val outputAudioFormat: String? = "pcm16",
    @SerialName("input_audio_transcription")
    val inputAudioTranscription: site.addzero.api.openai.models.RealtimeSessionInputAudioTranscription? = null,
    @SerialName("turn_detection")
    val turnDetection: site.addzero.api.openai.models.RealtimeTurnDetection? = null,
    /**
     * Configuration for input audio noise reduction. This can be set to `null` to turn off. Noise
     * reduction filters audio added to the input audio buffer before it is sent to VAD and the model.
     * Filtering the audio can improve VAD and turn detection accuracy (reducing false positives) and model
     * performance by improving perception of the input audio.
     */
    @SerialName("input_audio_noise_reduction")
    val inputAudioNoiseReduction: site.addzero.api.openai.models.RealtimeSessionInputAudioNoiseReduction? = null,
    /**
     * The speed of the model's spoken response. 1.0 is the default speed. 0.25 is the minimum speed. 1.5
     * is the maximum speed. This value can only be changed in between model turns, not while a response is
     * in progress.
     */
    val speed: Double? = 1.0,
    val tracing: JsonElement? = null,
    /**
     * Tools (functions) available to the model.
     */
    val tools: List<site.addzero.api.openai.models.RealtimeFunctionTool>? = null,
    /**
     * How the model chooses tools. Options are `auto`, `none`, `required`, or specify a function.
     */
    @SerialName("tool_choice")
    val toolChoice: String? = "auto",
    /**
     * Sampling temperature for the model, limited to [0.6, 1.2]. For audio models a temperature of 0.8 is
     * highly recommended for best performance.
     */
    val temperature: Double? = 0.8,
    /**
     * Maximum number of output tokens for a single assistant response, inclusive of tool calls. Provide an
     * integer between 1 and 4096 to limit output tokens, or `inf` for the maximum available tokens for a
     * given model. Defaults to `inf`.
     */
    @SerialName("max_response_output_tokens")
    val maxResponseOutputTokens: JsonElement? = null,
    /**
     * Expiration timestamp for the session, in seconds since epoch.
     */
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    val prompt: site.addzero.api.openai.models.Prompt? = null,
    val include: List<String>? = null
)
