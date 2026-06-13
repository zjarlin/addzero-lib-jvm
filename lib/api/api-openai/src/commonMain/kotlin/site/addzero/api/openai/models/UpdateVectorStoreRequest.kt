// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateVectorStoreRequest(
    /**
     * The name of the vector store.
     */
    val name: String? = null,
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.UpdateVectorStoreRequestExpiresAfter? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null
)
