// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Action type "open_page" - Opens a specific URL from search results.
 */
@Serializable
data class WebSearchActionOpenPage(
    /**
     * The action type.
     */
    val type: String,
    /**
     * The URL opened by the model.
     */
    val url: String? = null
)
