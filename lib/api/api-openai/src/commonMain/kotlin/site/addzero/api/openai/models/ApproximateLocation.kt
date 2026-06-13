// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ApproximateLocation(
    /**
     * The type of location approximation. Always `approximate`.
     */
    val type: String = "approximate",
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    val timezone: String? = null
)
