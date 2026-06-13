// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateChatCompletionRequest(
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    @SerialName("top_logprobs")
    val topLogprobs: Int? = null,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    /**
     * This field is being replaced by `safety_identifier` and `prompt_cache_key`. Use `prompt_cache_key`
     * instead to maintain caching optimizations. A stable identifier for your end-users. Used to boost
     * cache hit rates by better bucketing similar requests and to help OpenAI detect and prevent abuse.
     * [Learn more](/docs/guides/safety-best-practices#safety-identifiers).
     */
    val user: String? = null,
    /**
     * A stable identifier used to help detect users of your application that may be violating OpenAI's
     * usage policies. The IDs should be a string that uniquely identifies each user, with a maximum length
     * of 64 characters. We recommend hashing their username or email address, in order to avoid sending us
     * any identifying information. [Learn more](/docs/guides/safety-best-practices#safety-identifiers).
     */
    @SerialName("safety_identifier")
    val safetyIdentifier: String? = null,
    /**
     * Used by OpenAI to cache responses for similar requests to optimize your cache hit rates. Replaces
     * the `user` field. [Learn more](/docs/guides/prompt-caching).
     */
    @SerialName("prompt_cache_key")
    val promptCacheKey: String? = null,
    @SerialName("service_tier")
    val serviceTier: site.addzero.api.openai.models.ServiceTier? = null,
    @SerialName("prompt_cache_retention")
    val promptCacheRetention: String? = null,
    /**
     * A list of messages comprising the conversation so far. Depending on the [model](/docs/models) you
     * use, different message types (modalities) are supported, like [text](/docs/guides/text-generation),
     * [images](/docs/guides/vision), and [audio](/docs/guides/audio).
     */
    val messages: List<site.addzero.api.openai.models.ChatCompletionRequestMessage>,
    /**
     * Model ID used to generate the response, like `gpt-4o` or `o3`. OpenAI offers a wide range of models
     * with different capabilities, performance characteristics, and price points. Refer to the [model
     * guide](/docs/models) to browse and compare available models.
     */
    val model: site.addzero.api.openai.models.ModelIdsShared,
    val modalities: site.addzero.api.openai.models.ResponseModalities? = null,
    val verbosity: site.addzero.api.openai.models.Verbosity? = null,
    @SerialName("reasoning_effort")
    val reasoningEffort: site.addzero.api.openai.models.ReasoningEffort? = null,
    /**
     * An upper bound for the number of tokens that can be generated for a completion, including visible
     * output tokens and [reasoning tokens](/docs/guides/reasoning).
     */
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null,
    /**
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency
     * in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
     */
    @SerialName("frequency_penalty")
    val frequencyPenalty: Double? = 0.0,
    /**
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the
     * text so far, increasing the model's likelihood to talk about new topics.
     */
    @SerialName("presence_penalty")
    val presencePenalty: Double? = 0.0,
    /**
     * This tool searches the web for relevant results to use in a response. Learn more about the [web
     * search tool](/docs/guides/tools-web-search?api-mode=chat).
     */
    @SerialName("web_search_options")
    val webSearchOptions: site.addzero.api.openai.models.CreateChatCompletionRequestWebSearchOptions? = null,
    /**
     * An object specifying the format that the model must output. Setting to `{ "type": "json_schema",
     * "json_schema": {...} }` enables Structured Outputs which ensures the model will match your supplied
     * JSON schema. Learn more in the [Structured Outputs guide](/docs/guides/structured-outputs). Setting
     * to `{ "type": "json_object" }` enables the older JSON mode, which ensures the message the model
     * generates is valid JSON. Using `json_schema` is preferred for models that support it.
     */
    @SerialName("response_format")
    val responseFormat: JsonElement? = null,
    /**
     * Parameters for audio output. Required when audio output is requested with `modalities: ["audio"]`.
     * [Learn more](/docs/guides/audio).
     */
    val audio: site.addzero.api.openai.models.CreateChatCompletionRequestAudio? = null,
    /**
     * Whether or not to store the output of this chat completion request for use in our [model
     * distillation](/docs/guides/distillation) or [evals](/docs/guides/evals) products. Supports text and
     * image inputs. Note: image inputs over 8MB will be dropped.
     */
    val store: Boolean? = false,
    /**
     * If set to true, the model response data will be streamed to the client as it is generated using
     * [server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-
     * sent_events/Using_server-sent_events#Event_stream_format). See the [Streaming section
     * below](/docs/api-reference/chat/streaming) for more information, along with the [streaming
     * responses](/docs/guides/streaming-responses) guide for more information on how to handle the
     * streaming events.
     */
    val stream: Boolean? = false,
    val stop: site.addzero.api.openai.models.StopConfiguration? = null,
    /**
     * Modify the likelihood of specified tokens appearing in the completion. Accepts a JSON object that
     * maps tokens (specified by their token ID in the tokenizer) to an associated bias value from -100 to
     * 100. Mathematically, the bias is added to the logits generated by the model prior to sampling. The
     * exact effect will vary per model, but values between -1 and 1 should decrease or increase likelihood
     * of selection; values like -100 or 100 should result in a ban or exclusive selection of the relevant
     * token.
     */
    @SerialName("logit_bias")
    val logitBias: Map<String, Int>? = null,
    /**
     * Whether to return log probabilities of the output tokens or not. If true, returns the log
     * probabilities of each output token returned in the `content` of `message`.
     */
    val logprobs: Boolean? = false,
    /**
     * The maximum number of [tokens](/tokenizer) that can be generated in the chat completion. This value
     * can be used to control [costs](https://openai.com/api/pricing/) for text generated via API. This
     * value is now deprecated in favor of `max_completion_tokens`, and is not compatible with [o-series
     * models](/docs/guides/reasoning).
     */
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    /**
     * How many chat completion choices to generate for each input message. Note that you will be charged
     * based on the number of generated tokens across all of the choices. Keep `n` as `1` to minimize
     * costs.
     */
    val n: Int? = 1,
    /**
     * Configuration for a [Predicted Output](/docs/guides/predicted-outputs), which can greatly improve
     * response times when large parts of the model response are known ahead of time. This is most common
     * when you are regenerating a file with only minor changes to most of the content.
     */
    val prediction: site.addzero.api.openai.models.PredictionContent? = null,
    /**
     * This feature is in Beta. If specified, our system will make a best effort to sample
     * deterministically, such that repeated requests with the same `seed` and parameters should return the
     * same result. Determinism is not guaranteed, and you should refer to the `system_fingerprint`
     * response parameter to monitor changes in the backend.
     */
    val seed: Int? = null,
    @SerialName("stream_options")
    val streamOptions: site.addzero.api.openai.models.ChatCompletionStreamOptions? = null,
    /**
     * A list of tools the model may call. You can provide either [custom tools](/docs/guides/function-
     * calling#custom-tools) or [function tools](/docs/guides/function-calling).
     */
    val tools: List<JsonElement>? = null,
    @SerialName("tool_choice")
    val toolChoice: site.addzero.api.openai.models.ChatCompletionToolChoiceOption? = null,
    @SerialName("parallel_tool_calls")
    val parallelToolCalls: site.addzero.api.openai.models.ParallelToolCalls? = null,
    /**
     * Deprecated in favor of `tool_choice`. Controls which (if any) function is called by the model.
     * `none` means the model will not call a function and instead generates a message. `auto` means the
     * model can pick between generating a message or calling a function. Specifying a particular function
     * via `{"name": "my_function"}` forces the model to call that function. `none` is the default when no
     * functions are present. `auto` is the default if functions are present.
     */
    @SerialName("function_call")
    val functionCall: JsonElement? = null,
    /**
     * Deprecated in favor of `tools`. A list of functions the model may generate JSON inputs for.
     */
    val functions: List<site.addzero.api.openai.models.ChatCompletionFunctions>? = null
)
