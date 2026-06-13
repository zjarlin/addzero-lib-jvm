// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `fine_tuning.job.checkpoint` object represents a model checkpoint for a fine-tuning job that is
 * ready to use.
 */
@Serializable
data class FineTuningJobCheckpoint(
    /**
     * The checkpoint identifier, which can be referenced in the API endpoints.
     */
    val id: String,
    /**
     * The Unix timestamp (in seconds) for when the checkpoint was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The name of the fine-tuned checkpoint model that is created.
     */
    @SerialName("fine_tuned_model_checkpoint")
    val fineTunedModelCheckpoint: String,
    /**
     * The step number that the checkpoint was created at.
     */
    @SerialName("step_number")
    val stepNumber: Int,
    /**
     * Metrics at the step number during the fine-tuning job.
     */
    val metrics: site.addzero.api.openai.models.FineTuningJobCheckpointMetrics,
    /**
     * The name of the fine-tuning job that this checkpoint was created from.
     */
    @SerialName("fine_tuning_job_id")
    val fineTuningJobId: String,
    /**
     * The object type, which is always "fine_tuning.job.checkpoint".
     */
    @SerialName("object")
    val objectType: String
)
