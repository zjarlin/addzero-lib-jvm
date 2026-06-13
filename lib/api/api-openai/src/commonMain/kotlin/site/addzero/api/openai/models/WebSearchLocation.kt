// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Approximate location parameters for the search.
 */
@Serializable
data class WebSearchLocation(
    /**
     * The two-letter [ISO country code](https://en.wikipedia.org/wiki/ISO_3166-1) of the user, e.g. `US`.
     */
    val country: String? = null,
    /**
     * Free text input for the region of the user, e.g. `California`.
     */
    val region: String? = null,
    /**
     * Free text input for the city of the user, e.g. `San Francisco`.
     */
    val city: String? = null,
    /**
     * The [IANA timezone](https://timeapi.io/documentation/iana-timezones) of the user, e.g.
     * `America/Los_Angeles`.
     */
    val timezone: String? = null
)
