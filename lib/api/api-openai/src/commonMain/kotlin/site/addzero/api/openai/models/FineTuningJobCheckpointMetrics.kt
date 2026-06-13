// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Metrics at the step number during the fine-tuning job.
 */
@Serializable
data class FineTuningJobCheckpointMetrics(
    val step: Double? = null,
    @SerialName("train_loss")
    val trainLoss: Double? = null,
    @SerialName("train_mean_token_accuracy")
    val trainMeanTokenAccuracy: Double? = null,
    @SerialName("valid_loss")
    val validLoss: Double? = null,
    @SerialName("valid_mean_token_accuracy")
    val validMeanTokenAccuracy: Double? = null,
    @SerialName("full_valid_loss")
    val fullValidLoss: Double? = null,
    @SerialName("full_valid_mean_token_accuracy")
    val fullValidMeanTokenAccuracy: Double? = null
)
