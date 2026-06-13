// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Create a new Realtime response with these parameters
 */
@Serializable
data class RealtimeBetaResponseCreateParams(
    /**
     * The set of modalities the model can respond with. To disable audio, set this to ["text"].
     */
    val modalities: List<String>? = null,
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
     * The format of output audio. Options are `pcm16`, `g711_ulaw`, or `g711_alaw`.
     */
    @SerialName("output_audio_format")
    val outputAudioFormat: String? = null,
    /**
     * Tools (functions) available to the model.
     */
    val tools: List<site.addzero.api.openai.models.RealtimeBetaResponseCreateParamsTool>? = null,
    /**
     * How the model chooses tools. Provide one of the string modes or force a specific function/MCP tool.
     */
    @SerialName("tool_choice")
    val toolChoice: JsonElement? = null,
    /**
     * Sampling temperature for the model, limited to [0.6, 1.2]. Defaults to 0.8.
     */
    val temperature: Double? = null,
    /**
     * Maximum number of output tokens for a single assistant response, inclusive of tool calls. Provide an
     * integer between 1 and 4096 to limit output tokens, or `inf` for the maximum available tokens for a
     * given model. Defaults to `inf`.
     */
    @SerialName("max_output_tokens")
    val maxOutputTokens: JsonElement? = null,
    /**
     * Controls which conversation the response is added to. Currently supports `auto` and `none`, with
     * `auto` as the default value. The `auto` value means that the contents of the response will be added
     * to the default conversation. Set this to `none` to create an out-of-band response which will not add
     * items to default conversation.
     */
    val conversation: String? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    val prompt: site.addzero.api.openai.models.Prompt? = null,
    /**
     * Input items to include in the prompt for the model. Using this field creates a new context for this
     * Response instead of using the default conversation. An empty array `[]` will clear the context for
     * this Response. Note that this can include references to items from the default conversation.
     */
    val input: List<site.addzero.api.openai.models.RealtimeConversationItem>? = null
)
