// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class VectorStoreFileContentResponseDataItem(
    /**
     * The content type (currently only `"text"`)
     */
    val type: String? = null,
    /**
     * The text content
     */
    val text: String? = null
)
