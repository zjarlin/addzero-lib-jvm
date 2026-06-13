// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The hyperparameters used for the fine-tuning job. This value will only be returned when running
 * `supervised` jobs.
 */
@Serializable
data class FineTuningJobHyperparameters(
    @SerialName("batch_size")
    val batchSize: JsonElement? = null,
    /**
     * Scaling factor for the learning rate. A smaller learning rate may be useful to avoid overfitting.
     */
    @SerialName("learning_rate_multiplier")
    val learningRateMultiplier: JsonElement? = null,
    /**
     * The number of epochs to train the model for. An epoch refers to one full cycle through the training
     * dataset.
     */
    @SerialName("n_epochs")
    val nEpochs: JsonElement? = null
)
