// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectServiceAccountApiKey(
    /**
     * The object type, which is always `organization.project.service_account.api_key`
     */
    @SerialName("object")
    val objectType: String,
    val value: String,
    val name: String,
    @SerialName("created_at")
    val createdAt: Long,
    val id: String
)
