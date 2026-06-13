// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Configuration for the reinforcement fine-tuning method.
 */
@Serializable
data class FineTuneReinforcementMethod(
    /**
     * The grader used for the fine-tuning job.
     */
    val grader: JsonElement,
    val hyperparameters: site.addzero.api.openai.models.FineTuneReinforcementHyperparameters? = null
)
