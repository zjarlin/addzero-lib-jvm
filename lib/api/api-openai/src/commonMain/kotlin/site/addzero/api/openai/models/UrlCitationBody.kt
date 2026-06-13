// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A citation for a web resource used to generate a model response.
 */
@Serializable
data class UrlCitationBody(
    /**
     * The type of the URL citation. Always `url_citation`.
     */
    val type: String = "url_citation",
    /**
     * The URL of the web resource.
     */
    val url: String,
    /**
     * The index of the first character of the URL citation in the message.
     */
    @SerialName("start_index")
    val startIndex: Int,
    /**
     * The index of the last character of the URL citation in the message.
     */
    @SerialName("end_index")
    val endIndex: Int,
    /**
     * The title of the web resource.
     */
    val title: String
)
