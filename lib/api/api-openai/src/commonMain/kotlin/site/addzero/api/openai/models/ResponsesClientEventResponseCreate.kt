// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Client event for creating a response over a persistent WebSocket connection. This payload uses the
 * same top-level fields as `POST /v1/responses`. Notes: - `stream` is implicit over WebSocket and
 * should not be sent. - `background` is not supported over WebSocket.
 */
@Serializable
data class ResponsesClientEventResponseCreate(
    /**
     * The type of the client event. Always `response.create`.
     */
    val type: String,
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
    @SerialName("previous_response_id")
    val previousResponseId: String? = null,
    /**
     * Model ID used to generate the response, like `gpt-4o` or `o3`. OpenAI offers a wide range of models
     * with different capabilities, performance characteristics, and price points. Refer to the [model
     * guide](/docs/models) to browse and compare available models.
     */
    val model: site.addzero.api.openai.models.ModelIdsResponses? = null,
    val reasoning: site.addzero.api.openai.models.Reasoning? = null,
    val background: Boolean? = null,
    @SerialName("max_tool_calls")
    val maxToolCalls: Int? = null,
    val text: site.addzero.api.openai.models.ResponseTextParam? = null,
    val tools: site.addzero.api.openai.models.ToolsArray? = null,
    @SerialName("tool_choice")
    val toolChoice: site.addzero.api.openai.models.ToolChoiceParam? = null,
    val prompt: site.addzero.api.openai.models.Prompt? = null,
    val truncation: String? = null,
    val input: site.addzero.api.openai.models.InputParam? = null,
    val include: List<site.addzero.api.openai.models.IncludeEnum>? = null,
    @SerialName("parallel_tool_calls")
    val parallelToolCalls: Boolean? = null,
    val store: Boolean? = null,
    val instructions: String? = null,
    val stream: Boolean? = null,
    @SerialName("stream_options")
    val streamOptions: site.addzero.api.openai.models.ResponseStreamOptions? = null,
    val conversation: site.addzero.api.openai.models.ConversationParam? = null,
    @SerialName("context_management")
    val contextManagement: List<site.addzero.api.openai.models.ContextManagementParam>? = null,
    @SerialName("max_output_tokens")
    val maxOutputTokens: Int? = null
)
