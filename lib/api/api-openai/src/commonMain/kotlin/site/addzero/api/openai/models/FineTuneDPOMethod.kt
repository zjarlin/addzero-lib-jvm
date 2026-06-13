// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for the DPO fine-tuning method.
 */
@Serializable
data class FineTuneDPOMethod(
    val hyperparameters: site.addzero.api.openai.models.FineTuneDPOHyperparameters? = null
)
