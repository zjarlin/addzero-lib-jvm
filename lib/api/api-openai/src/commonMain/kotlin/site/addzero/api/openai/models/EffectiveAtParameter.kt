// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class EffectiveAtParameter(
    /**
     * Return only events whose `effective_at` (Unix seconds) is greater than this value.
     */
    val gt: Int? = null,
    /**
     * Return only events whose `effective_at` (Unix seconds) is greater than or equal to this value.
     */
    val gte: Int? = null,
    /**
     * Return only events whose `effective_at` (Unix seconds) is less than this value.
     */
    val lt: Int? = null,
    /**
     * Return only events whose `effective_at` (Unix seconds) is less than or equal to this value.
     */
    val lte: Int? = null
)
