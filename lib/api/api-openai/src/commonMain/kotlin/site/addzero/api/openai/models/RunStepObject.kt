// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a step in execution of a run.
 */
@Serializable
data class RunStepObject(
    /**
     * The identifier of the run step, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `thread.run.step`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The Unix timestamp (in seconds) for when the run step was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The ID of the [assistant](/docs/api-reference/assistants) associated with the run step.
     */
    @SerialName("assistant_id")
    val assistantId: String,
    /**
     * The ID of the [thread](/docs/api-reference/threads) that was run.
     */
    @SerialName("thread_id")
    val threadId: String,
    /**
     * The ID of the [run](/docs/api-reference/runs) that this run step is a part of.
     */
    @SerialName("run_id")
    val runId: String,
    /**
     * The type of run step, which can be either `message_creation` or `tool_calls`.
     */
    val type: String,
    /**
     * The status of the run step, which can be either `in_progress`, `cancelled`, `failed`, `completed`,
     * or `expired`.
     */
    val status: String,
    /**
     * The details of the run step.
     */
    @SerialName("step_details")
    val stepDetails: JsonElement,
    @SerialName("last_error")
    val lastError: site.addzero.api.openai.models.RunStepObjectLastError?,
    @SerialName("expired_at")
    val expiredAt: Long?,
    @SerialName("cancelled_at")
    val cancelledAt: Long?,
    @SerialName("failed_at")
    val failedAt: Long?,
    @SerialName("completed_at")
    val completedAt: Long?,
    val metadata: site.addzero.api.openai.models.Metadata?,
    val usage: site.addzero.api.openai.models.RunStepCompletionUsage?
)
