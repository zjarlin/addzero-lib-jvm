// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectServiceAccountCreateResponse(
    @SerialName("object")
    val objectType: String,
    val id: String,
    val name: String,
    /**
     * Service accounts can only have one role of type `member`
     */
    val role: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("api_key")
    val apiKey: site.addzero.api.openai.models.ProjectServiceAccountApiKey?
)
