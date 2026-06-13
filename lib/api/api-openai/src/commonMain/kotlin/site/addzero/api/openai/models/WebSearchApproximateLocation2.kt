// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The approximate location of the user.
 */
@Serializable
data class WebSearchApproximateLocation2(
    /**
     * The type of location approximation. Always `approximate`.
     */
    val type: String? = "approximate",
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    val timezone: String? = null
)
