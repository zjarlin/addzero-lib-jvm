// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The results of a web search tool call. See the [web search guide](/docs/guides/tools-web-search) for
 * more information.
 */
@Serializable
data class WebSearchToolCall(
    /**
     * The unique ID of the web search tool call.
     */
    val id: String,
    /**
     * The type of the web search tool call. Always `web_search_call`.
     */
    val type: String,
    /**
     * The status of the web search tool call.
     */
    val status: String,
    /**
     * An object describing the specific action taken in this web search call. Includes details on how the
     * model used the web (search, open_page, find_in_page).
     */
    val action: JsonElement
)
