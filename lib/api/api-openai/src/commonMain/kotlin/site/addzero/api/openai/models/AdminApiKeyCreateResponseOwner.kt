// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminApiKeyCreateResponseOwner(
    /**
     * Always `user`
     */
    val type: String? = null,
    /**
     * The object type, which is always organization.user
     */
    @SerialName("object")
    val objectType: String? = null,
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String? = null,
    /**
     * The name of the user
     */
    val name: String? = null,
    /**
     * The Unix timestamp (in seconds) of when the user was created
     */
    @SerialName("created_at")
    val createdAt: Long? = null,
    /**
     * Always `owner`
     */
    val role: String? = null
)
