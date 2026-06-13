// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class VectorStoreSearchResultContentObject(
    /**
     * The type of content.
     */
    val type: String,
    /**
     * The text content returned from search.
     */
    val text: String
)
