// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Realtime session object configuration.
 */
@Serializable
data class RealtimeSessionCreateRequestGA(
    /**
     * The type of session to create. Always `realtime` for the Realtime API.
     */
    val type: String,
    /**
     * The set of modalities the model can respond with. It defaults to `["audio"]`, indicating that the
     * model will respond with audio plus a transcript. `["text"]` can be used to make the model respond
     * with text only. It is not possible to request both `text` and `audio` at the same time.
     */
    @SerialName("output_modalities")
    val outputModalities: List<String>? = null,
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
     * Configuration for input and output audio.
     */
    val audio: site.addzero.api.openai.models.RealtimeSessionCreateRequestGAAudio? = null,
    /**
     * Additional fields to include in server outputs. `item.input_audio_transcription.logprobs`: Include
     * logprobs for input audio transcription.
     */
    val include: List<String>? = null,
    /**
     * Realtime API can write session traces to the [Traces
     * Dashboard](https://platform.openai.com/logs?api=traces). Set to null to disable tracing. Once
     * tracing is enabled for a session, the configuration cannot be modified. `auto` will create a trace
     * for the session with default values for the workflow name, group id, and metadata.
     */
    val tracing: JsonElement? = null,
    /**
     * Tools available to the model.
     */
    val tools: List<JsonElement>? = null,
    /**
     * How the model chooses tools. Provide one of the string modes or force a specific function/MCP tool.
     */
    @SerialName("tool_choice")
    val toolChoice: JsonElement? = null,
    /**
     * Whether the model may call multiple tools in parallel. Only supported by reasoning Realtime models
     * such as `gpt-realtime-2`.
     */
    @SerialName("parallel_tool_calls")
    val parallelToolCalls: Boolean? = null,
    val reasoning: site.addzero.api.openai.models.RealtimeReasoning? = null,
    /**
     * Maximum number of output tokens for a single assistant response, inclusive of tool calls. Provide an
     * integer between 1 and 4096 to limit output tokens, or `inf` for the maximum available tokens for a
     * given model. Defaults to `inf`.
     */
    @SerialName("max_output_tokens")
    val maxOutputTokens: JsonElement? = null,
    val truncation: site.addzero.api.openai.models.RealtimeTruncation? = null,
    val prompt: site.addzero.api.openai.models.Prompt? = null
)
