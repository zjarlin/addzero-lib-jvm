// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The method used for fine-tuning.
 */
@Serializable
data class FineTuneMethod(
    /**
     * The type of method. Is either `supervised`, `dpo`, or `reinforcement`.
     */
    val type: String,
    val supervised: site.addzero.api.openai.models.FineTuneSupervisedMethod? = null,
    val dpo: site.addzero.api.openai.models.FineTuneDPOMethod? = null,
    val reinforcement: site.addzero.api.openai.models.FineTuneReinforcementMethod? = null
)
