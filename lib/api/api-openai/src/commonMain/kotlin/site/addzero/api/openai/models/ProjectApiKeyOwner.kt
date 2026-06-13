// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectApiKeyOwner(
    /**
     * `user` or `service_account`
     */
    val type: String? = null,
    val user: site.addzero.api.openai.models.ProjectApiKeyOwnerUser? = null,
    @SerialName("service_account")
    val serviceAccount: site.addzero.api.openai.models.ProjectApiKeyOwnerServiceAccount? = null
)
