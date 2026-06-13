// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateVectorStoreRequest(
    /**
     * A list of [File](/docs/api-reference/files) IDs that the vector store should use. Useful for tools
     * like `file_search` that can access files.
     */
    @SerialName("file_ids")
    val fileIds: List<String>? = null,
    /**
     * The name of the vector store.
     */
    val name: String? = null,
    /**
     * A description for the vector store. Can be used to describe the vector store's purpose.
     */
    val description: String? = null,
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.VectorStoreExpirationAfter? = null,
    /**
     * The chunking strategy used to chunk the file(s). If not set, will use the `auto` strategy. Only
     * applicable if `file_ids` is non-empty.
     */
    @SerialName("chunking_strategy")
    val chunkingStrategy: JsonElement? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null
)
