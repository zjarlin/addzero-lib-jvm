// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadObjectToolResourcesFileSearch(
    /**
     * The [vector store](/docs/api-reference/vector-stores/object) attached to this thread. There can be a
     * maximum of 1 vector store attached to the thread.
     */
    @SerialName("vector_store_ids")
    val vectorStoreIds: List<String>? = null
)
