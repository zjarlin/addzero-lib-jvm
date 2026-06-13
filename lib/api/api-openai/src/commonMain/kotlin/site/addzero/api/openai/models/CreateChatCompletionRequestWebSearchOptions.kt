// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This tool searches the web for relevant results to use in a response. Learn more about the [web
 * search tool](/docs/guides/tools-web-search?api-mode=chat).
 */
@Serializable
data class CreateChatCompletionRequestWebSearchOptions(
    /**
     * Approximate location parameters for the search.
     */
    @SerialName("user_location")
    val userLocation: site.addzero.api.openai.models.CreateChatCompletionRequestWebSearchOptionsUserLocation? = null,
    @SerialName("search_context_size")
    val searchContextSize: site.addzero.api.openai.models.WebSearchContextSize? = null
)
