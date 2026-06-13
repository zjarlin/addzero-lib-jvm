// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A new Realtime session configuration, with an ephemeral key. Default TTL for keys is one minute.
 */
@Serializable
data class RealtimeSessionCreateRequest(
    /**
     * Ephemeral key returned by the API.
     */
    @SerialName("client_secret")
    val clientSecret: site.addzero.api.openai.models.RealtimeSessionCreateRequestClientSecret,
    /**
     * The set of modalities the model can respond with. To disable audio, set this to ["text"].
     */
    val modalities: JsonElement? = null,
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
     * The voice the model uses to respond. Supported built-in voices are `alloy`, `ash`, `ballad`,
     * `coral`, `echo`, `sage`, `shimmer`, `verse`, `marin`, and `cedar`. You may also provide a custom
     * voice object with an `id`, for example `{ "id": "voice_1234" }`. Voice cannot be changed during the
     * session once the model has responded with audio at least once.
     */
    val voice: site.addzero.api.openai.models.VoiceIdsOrCustomVoice? = null,
    /**
     * The format of input audio. Options are `pcm16`, `g711_ulaw`, or `g711_alaw`.
     */
    @SerialName("input_audio_format")
    val inputAudioFormat: String? = null,
    /**
     * The format of output audio. Options are `pcm16`, `g711_ulaw`, or `g711_alaw`.
     */
    @SerialName("output_audio_format")
    val outputAudioFormat: String? = null,
    /**
     * Configuration for input audio transcription, defaults to off and can be set to `null` to turn off
     * once on. Input audio transcription is not native to the model, since the model consumes audio
     * directly. Transcription runs asynchronously and should be treated as rough guidance rather than the
     * representation understood by the model.
     */
    @SerialName("input_audio_transcription")
    val inputAudioTranscription: site.addzero.api.openai.models.RealtimeSessionCreateRequestInputAudioTranscription? = null,
    /**
     * The speed of the model's spoken response. 1.0 is the default speed. 0.25 is the minimum speed. 1.5
     * is the maximum speed. This value can only be changed in between model turns, not while a response is
     * in progress.
     */
    val speed: Double? = 1.0,
    /**
     * Configuration options for tracing. Set to null to disable tracing. Once tracing is enabled for a
     * session, the configuration cannot be modified. `auto` will create a trace for the session with
     * default values for the workflow name, group id, and metadata.
     */
    val tracing: JsonElement? = null,
    /**
     * Configuration for turn detection. Can be set to `null` to turn off. Server VAD means that the model
     * will detect the start and end of speech based on audio volume and respond at the end of user speech.
     */
    @SerialName("turn_detection")
    val turnDetection: site.addzero.api.openai.models.RealtimeSessionCreateRequestTurnDetection? = null,
    /**
     * Tools (functions) available to the model.
     */
    val tools: List<site.addzero.api.openai.models.RealtimeSessionCreateRequestTool>? = null,
    /**
     * How the model chooses tools. Options are `auto`, `none`, `required`, or specify a function.
     */
    @SerialName("tool_choice")
    val toolChoice: String? = null,
    /**
     * Sampling temperature for the model, limited to [0.6, 1.2]. Defaults to 0.8.
     */
    val temperature: Double? = null,
    /**
     * Maximum number of output tokens for a single assistant response, inclusive of tool calls. Provide an
     * integer between 1 and 4096 to limit output tokens, or `inf` for the maximum available tokens for a
     * given model. Defaults to `inf`.
     */
    @SerialName("max_response_output_tokens")
    val maxResponseOutputTokens: JsonElement? = null,
    val truncation: site.addzero.api.openai.models.RealtimeTruncation? = null,
    val prompt: site.addzero.api.openai.models.Prompt? = null
)
