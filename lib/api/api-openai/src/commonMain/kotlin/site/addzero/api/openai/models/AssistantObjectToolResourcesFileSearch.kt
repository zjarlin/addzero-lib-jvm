// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssistantObjectToolResourcesFileSearch(
    /**
     * The ID of the [vector store](/docs/api-reference/vector-stores/object) attached to this assistant.
     * There can be a maximum of 1 vector store attached to the assistant.
     */
    @SerialName("vector_store_ids")
    val vectorStoreIds: List<String>? = null
)
