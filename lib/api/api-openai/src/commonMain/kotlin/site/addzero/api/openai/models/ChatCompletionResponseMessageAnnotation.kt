// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A URL citation when using web search.
 */
@Serializable
data class ChatCompletionResponseMessageAnnotation(
    /**
     * The type of the URL citation. Always `url_citation`.
     */
    val type: String,
    /**
     * A URL citation when using web search.
     */
    @SerialName("url_citation")
    val urlCitation: site.addzero.api.openai.models.ChatCompletionResponseMessageAnnotationUrlCitation
)
