// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This tool searches the web for relevant results to use in a response. Learn more about the [web
 * search tool](https://platform.openai.com/docs/guides/tools-web-search).
 */
@Serializable
data class WebSearchPreviewTool(
  /**
     * The type of the web search tool. One of `web_search_preview` or `web_search_preview_2025_03_11`.
     */
    val type: String = "web_search_preview",
  @SerialName("user_location")
    val userLocation: site.addzero.api.openai.models.ApproximateLocation? = null,
  /**
     * High level guidance for the amount of context window space to use for the search. One of `low`,
     * `medium`, or `high`. `medium` is the default.
     */
    @SerialName("search_context_size")
    val searchContextSize: site.addzero.api.openai.models.SearchContextSize? = null,
  @SerialName("search_content_types")
    val searchContentTypes: List<site.addzero.api.openai.models.SearchContentType>? = null
)
