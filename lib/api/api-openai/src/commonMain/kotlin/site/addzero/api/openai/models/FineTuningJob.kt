// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `fine_tuning.job` object represents a fine-tuning job that has been created through the API.
 */
@Serializable
data class FineTuningJob(
    /**
     * The object identifier, which can be referenced in the API endpoints.
     */
    val id: String,
    /**
     * The Unix timestamp (in seconds) for when the fine-tuning job was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    val error: site.addzero.api.openai.models.FineTuningJobError?,
    @SerialName("fine_tuned_model")
    val fineTunedModel: String?,
    @SerialName("finished_at")
    val finishedAt: Long?,
    /**
     * The hyperparameters used for the fine-tuning job. This value will only be returned when running
     * `supervised` jobs.
     */
    val hyperparameters: site.addzero.api.openai.models.FineTuningJobHyperparameters,
    /**
     * The base model that is being fine-tuned.
     */
    val model: String,
    /**
     * The object type, which is always "fine_tuning.job".
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The organization that owns the fine-tuning job.
     */
    @SerialName("organization_id")
    val organizationId: String,
    /**
     * The compiled results file ID(s) for the fine-tuning job. You can retrieve the results with the
     * [Files API](/docs/api-reference/files/retrieve-contents).
     */
    @SerialName("result_files")
    val resultFiles: List<String>,
    /**
     * The current status of the fine-tuning job, which can be either `validating_files`, `queued`,
     * `running`, `succeeded`, `failed`, or `cancelled`.
     */
    val status: String,
    @SerialName("trained_tokens")
    val trainedTokens: Int?,
    /**
     * The file ID used for training. You can retrieve the training data with the [Files API](/docs/api-
     * reference/files/retrieve-contents).
     */
    @SerialName("training_file")
    val trainingFile: String,
    @SerialName("validation_file")
    val validationFile: String?,
    val integrations: List<site.addzero.api.openai.models.FineTuningIntegration>? = null,
    /**
     * The seed used for the fine-tuning job.
     */
    val seed: Int,
    @SerialName("estimated_finish")
    val estimatedFinish: Long? = null,
    val method: site.addzero.api.openai.models.FineTuneMethod? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null
)
