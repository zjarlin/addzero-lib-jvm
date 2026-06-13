// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Action type "find_in_page": Searches for a pattern within a loaded page.
 */
@Serializable
data class WebSearchActionFind(
    /**
     * The action type.
     */
    val type: String,
    /**
     * The URL of the page searched for the pattern.
     */
    val url: String,
    /**
     * The pattern or text to search for within the page.
     */
    val pattern: String
)
