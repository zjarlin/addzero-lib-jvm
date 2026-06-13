// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the parsed content of a vector store file.
 */
@Serializable
data class VectorStoreFileContentResponse(
    /**
     * The object type, which is always `vector_store.file_content.page`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Parsed content of the file.
     */
    val data: List<site.addzero.api.openai.models.VectorStoreFileContentResponseDataItem>,
    /**
     * Indicates if there are more content pages to fetch.
     */
    @SerialName("has_more")
    val hasMore: Boolean,
    @SerialName("next_page")
    val nextPage: String?
)
