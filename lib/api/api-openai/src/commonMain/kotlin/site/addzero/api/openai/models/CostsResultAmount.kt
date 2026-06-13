// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The monetary value in its associated currency.
 */
@Serializable
data class CostsResultAmount(
    /**
     * The numeric value of the cost.
     */
    val value: Double? = null,
    /**
     * Lowercase ISO-4217 currency e.g. "usd"
     */
    val currency: String? = null
)
