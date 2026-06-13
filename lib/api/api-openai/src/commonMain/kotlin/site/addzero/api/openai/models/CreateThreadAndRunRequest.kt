// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateThreadAndRunRequest(
    /**
     * The ID of the [assistant](/docs/api-reference/assistants) to use to execute this run.
     */
    @SerialName("assistant_id")
    val assistantId: String,
    val thread: site.addzero.api.openai.models.CreateThreadRequest? = null,
    /**
     * The ID of the [Model](/docs/api-reference/models) to be used to execute this run. If a value is
     * provided here, it will override the model associated with the assistant. If not, the model
     * associated with the assistant will be used.
     */
    val model: String? = null,
    /**
     * Override the default system message of the assistant. This is useful for modifying the behavior on a
     * per-run basis.
     */
    val instructions: String? = null,
    /**
     * Override the tools the assistant can use for this run. This is useful for modifying the behavior on
     * a per-run basis.
     */
    val tools: List<JsonElement>? = null,
    /**
     * A set of resources that are used by the assistant's tools. The resources are specific to the type of
     * tool. For example, the `code_interpreter` tool requires a list of file IDs, while the `file_search`
     * tool requires a list of vector store IDs.
     */
    @SerialName("tool_resources")
    val toolResources: site.addzero.api.openai.models.CreateThreadAndRunRequestToolResources? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    /**
     * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more
     * random, while lower values like 0.2 will make it more focused and deterministic.
     */
    val temperature: Double? = 1.0,
    /**
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the
     * results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top
     * 10% probability mass are considered. We generally recommend altering this or temperature but not
     * both.
     */
    @SerialName("top_p")
    val topP: Double? = 1.0,
    /**
     * If `true`, returns a stream of events that happen during the Run as server-sent events, terminating
     * when the Run enters a terminal state with a `data: [DONE]` message.
     */
    val stream: Boolean? = null,
    /**
     * The maximum number of prompt tokens that may be used over the course of the run. The run will make a
     * best effort to use only the number of prompt tokens specified, across multiple turns of the run. If
     * the run exceeds the number of prompt tokens specified, the run will end with status `incomplete`.
     * See `incomplete_details` for more info.
     */
    @SerialName("max_prompt_tokens")
    val maxPromptTokens: Int? = null,
    /**
     * The maximum number of completion tokens that may be used over the course of the run. The run will
     * make a best effort to use only the number of completion tokens specified, across multiple turns of
     * the run. If the run exceeds the number of completion tokens specified, the run will end with status
     * `incomplete`. See `incomplete_details` for more info.
     */
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null,
    @SerialName("truncation_strategy")
    val truncationStrategy: site.addzero.api.openai.models.CreateThreadAndRunRequestTruncationStrategy? = null,
    @SerialName("tool_choice")
    val toolChoice: site.addzero.api.openai.models.CreateThreadAndRunRequestToolChoice? = null,
    @SerialName("parallel_tool_calls")
    val parallelToolCalls: site.addzero.api.openai.models.ParallelToolCalls? = null,
    @SerialName("response_format")
    val responseFormat: site.addzero.api.openai.models.AssistantsApiResponseFormatOption? = null
)
