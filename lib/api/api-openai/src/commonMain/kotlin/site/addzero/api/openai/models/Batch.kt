// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Batch(
    val id: String,
    /**
     * The object type, which is always `batch`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The OpenAI API endpoint used by the batch.
     */
    val endpoint: String,
    /**
     * Model ID used to process the batch, like `gpt-5-2025-08-07`. OpenAI offers a wide range of models
     * with different capabilities, performance characteristics, and price points. Refer to the [model
     * guide](/docs/models) to browse and compare available models.
     */
    val model: String? = null,
    val errors: site.addzero.api.openai.models.BatchErrors? = null,
    /**
     * The ID of the input file for the batch.
     */
    @SerialName("input_file_id")
    val inputFileId: String,
    /**
     * The time frame within which the batch should be processed.
     */
    @SerialName("completion_window")
    val completionWindow: String,
    /**
     * The current status of the batch.
     */
    val status: String,
    /**
     * The ID of the file containing the outputs of successfully executed requests.
     */
    @SerialName("output_file_id")
    val outputFileId: String? = null,
    /**
     * The ID of the file containing the outputs of requests with errors.
     */
    @SerialName("error_file_id")
    val errorFileId: String? = null,
    /**
     * The Unix timestamp (in seconds) for when the batch was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The Unix timestamp (in seconds) for when the batch started processing.
     */
    @SerialName("in_progress_at")
    val inProgressAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) for when the batch will expire.
     */
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) for when the batch started finalizing.
     */
    @SerialName("finalizing_at")
    val finalizingAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) for when the batch was completed.
     */
    @SerialName("completed_at")
    val completedAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) for when the batch failed.
     */
    @SerialName("failed_at")
    val failedAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) for when the batch expired.
     */
    @SerialName("expired_at")
    val expiredAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) for when the batch started cancelling.
     */
    @SerialName("cancelling_at")
    val cancellingAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) for when the batch was cancelled.
     */
    @SerialName("cancelled_at")
    val cancelledAt: Long? = null,
    /**
     * The request counts for different statuses within the batch.
     */
    @SerialName("request_counts")
    val requestCounts: site.addzero.api.openai.models.BatchRequestCounts? = null,
    /**
     * Represents token usage details including input tokens, output tokens, a breakdown of output tokens,
     * and the total tokens used. Only populated on batches created after September 7, 2025.
     */
    val usage: site.addzero.api.openai.models.BatchUsage? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null
)
