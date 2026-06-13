// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A URL citation when using web search.
 */
@Serializable
data class ChatCompletionMessageListDataItemAnnotationUrlCitation(
    /**
     * The index of the last character of the URL citation in the message.
     */
    @SerialName("end_index")
    val endIndex: Int,
    /**
     * The index of the first character of the URL citation in the message.
     */
    @SerialName("start_index")
    val startIndex: Int,
    /**
     * The URL of the web resource.
     */
    val url: String,
    /**
     * The title of the web resource.
     */
    val title: String
)
