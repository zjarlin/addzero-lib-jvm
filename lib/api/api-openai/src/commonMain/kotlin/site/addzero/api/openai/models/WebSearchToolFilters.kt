// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Filters for the search.
 */
@Serializable
data class WebSearchToolFilters(
    @SerialName("allowed_domains")
    val allowedDomains: List<String>? = null
)
