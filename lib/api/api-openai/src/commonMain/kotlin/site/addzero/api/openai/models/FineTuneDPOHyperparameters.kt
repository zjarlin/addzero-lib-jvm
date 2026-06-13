// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The hyperparameters used for the DPO fine-tuning job.
 */
@Serializable
data class FineTuneDPOHyperparameters(
    /**
     * The beta value for the DPO method. A higher beta value will increase the weight of the penalty
     * between the policy and reference model.
     */
    val beta: JsonElement? = null,
    /**
     * Number of examples in each batch. A larger batch size means that model parameters are updated less
     * frequently, but with lower variance.
     */
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
