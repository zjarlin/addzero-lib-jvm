// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Search the Internet for sources related to the prompt. Learn more about the [web search
 * tool](/docs/guides/tools-web-search).
 */
@Serializable
data class WebSearchTool(
  /**
     * The type of the web search tool. One of `web_search` or `web_search_2025_08_26`.
     */
    val type: String = "web_search",
  val filters: site.addzero.api.openai.models.WebSearchToolFilters? = null,
  @SerialName("user_location")
    val userLocation: site.addzero.api.openai.models.WebSearchApproximateLocation? = null,
  /**
     * High level guidance for the amount of context window space to use for the search. One of `low`,
     * `medium`, or `high`. `medium` is the default.
     */
    @SerialName("search_context_size")
    val searchContextSize: String? = "medium"
)
