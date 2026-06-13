// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The hyperparameters used for the reinforcement fine-tuning job.
 */
@Serializable
data class FineTuneReinforcementHyperparameters(
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
    val nEpochs: JsonElement? = null,
    /**
     * Level of reasoning effort.
     */
    @SerialName("reasoning_effort")
    val reasoningEffort: String? = "default",
    /**
     * Multiplier on amount of compute used for exploring search space during training.
     */
    @SerialName("compute_multiplier")
    val computeMultiplier: JsonElement? = null,
    /**
     * The number of training steps between evaluation runs.
     */
    @SerialName("eval_interval")
    val evalInterval: JsonElement? = null,
    /**
     * Number of evaluation samples to generate per training step.
     */
    @SerialName("eval_samples")
    val evalSamples: JsonElement? = null
)
