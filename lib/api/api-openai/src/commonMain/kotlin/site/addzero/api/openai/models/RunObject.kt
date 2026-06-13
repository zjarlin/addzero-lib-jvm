// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents an execution run on a [thread](/docs/api-reference/threads).
 */
@Serializable
data class RunObject(
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `thread.run`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The Unix timestamp (in seconds) for when the run was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The ID of the [thread](/docs/api-reference/threads) that was executed on as a part of this run.
     */
    @SerialName("thread_id")
    val threadId: String,
    /**
     * The ID of the [assistant](/docs/api-reference/assistants) used for execution of this run.
     */
    @SerialName("assistant_id")
    val assistantId: String,
    /**
     * The status of the run, which can be either `queued`, `in_progress`, `requires_action`, `cancelling`,
     * `cancelled`, `failed`, `completed`, `incomplete`, or `expired`.
     */
    val status: String,
    /**
     * Details on the action required to continue the run. Will be `null` if no action is required.
     */
    @SerialName("required_action")
    val requiredAction: site.addzero.api.openai.models.RunObjectRequiredAction?,
    /**
     * The last error associated with this run. Will be `null` if there are no errors.
     */
    @SerialName("last_error")
    val lastError: site.addzero.api.openai.models.RunObjectLastError?,
    /**
     * The Unix timestamp (in seconds) for when the run will expire.
     */
    @SerialName("expires_at")
    val expiresAt: Long?,
    /**
     * The Unix timestamp (in seconds) for when the run was started.
     */
    @SerialName("started_at")
    val startedAt: Long?,
    /**
     * The Unix timestamp (in seconds) for when the run was cancelled.
     */
    @SerialName("cancelled_at")
    val cancelledAt: Long?,
    /**
     * The Unix timestamp (in seconds) for when the run failed.
     */
    @SerialName("failed_at")
    val failedAt: Long?,
    /**
     * The Unix timestamp (in seconds) for when the run was completed.
     */
    @SerialName("completed_at")
    val completedAt: Long?,
    /**
     * Details on why the run is incomplete. Will be `null` if the run is not incomplete.
     */
    @SerialName("incomplete_details")
    val incompleteDetails: site.addzero.api.openai.models.RunObjectIncompleteDetails?,
    /**
     * The model that the [assistant](/docs/api-reference/assistants) used for this run.
     */
    val model: String,
    /**
     * The instructions that the [assistant](/docs/api-reference/assistants) used for this run.
     */
    val instructions: String,
    /**
     * The list of tools that the [assistant](/docs/api-reference/assistants) used for this run.
     */
    val tools: List<JsonElement>,
    val metadata: site.addzero.api.openai.models.Metadata?,
    val usage: site.addzero.api.openai.models.RunCompletionUsage?,
    /**
     * The sampling temperature used for this run. If not set, defaults to 1.
     */
    val temperature: Double? = null,
    /**
     * The nucleus sampling value used for this run. If not set, defaults to 1.
     */
    @SerialName("top_p")
    val topP: Double? = null,
    /**
     * The maximum number of prompt tokens specified to have been used over the course of the run.
     */
    @SerialName("max_prompt_tokens")
    val maxPromptTokens: Int?,
    /**
     * The maximum number of completion tokens specified to have been used over the course of the run.
     */
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int?,
    @SerialName("truncation_strategy")
    val truncationStrategy: site.addzero.api.openai.models.RunObjectTruncationStrategy,
    @SerialName("tool_choice")
    val toolChoice: site.addzero.api.openai.models.RunObjectToolChoice,
    @SerialName("parallel_tool_calls")
    val parallelToolCalls: site.addzero.api.openai.models.ParallelToolCalls,
    @SerialName("response_format")
    val responseFormat: site.addzero.api.openai.models.AssistantsApiResponseFormatOption?
)
