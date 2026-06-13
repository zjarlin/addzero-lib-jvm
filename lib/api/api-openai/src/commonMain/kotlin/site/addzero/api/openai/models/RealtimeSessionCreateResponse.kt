// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A Realtime session configuration object.
 */
@Serializable
data class RealtimeSessionCreateResponse(
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
     * Expiration timestamp for the session, in seconds since epoch.
     */
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    /**
     * Additional fields to include in server outputs. - `item.input_audio_transcription.logprobs`: Include
     * logprobs for input audio transcription.
     */
    val include: List<String>? = null,
    /**
     * The Realtime model used for this session.
     */
    val model: String? = null,
    /**
     * The set of modalities the model can respond with. To disable audio, set this to ["text"].
     */
    @SerialName("output_modalities")
    val outputModalities: JsonElement? = null,
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
     * Configuration for input and output audio for the session.
     */
    val audio: site.addzero.api.openai.models.RealtimeSessionCreateResponseAudio? = null,
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
    val turnDetection: site.addzero.api.openai.models.RealtimeSessionCreateResponseTurnDetection? = null,
    /**
     * Tools (functions) available to the model.
     */
    val tools: List<site.addzero.api.openai.models.RealtimeFunctionTool>? = null,
    /**
     * How the model chooses tools. Options are `auto`, `none`, `required`, or specify a function.
     */
    @SerialName("tool_choice")
    val toolChoice: String? = null,
    /**
     * Maximum number of output tokens for a single assistant response, inclusive of tool calls. Provide an
     * integer between 1 and 4096 to limit output tokens, or `inf` for the maximum available tokens for a
     * given model. Defaults to `inf`.
     */
    @SerialName("max_output_tokens")
    val maxOutputTokens: JsonElement? = null
)
