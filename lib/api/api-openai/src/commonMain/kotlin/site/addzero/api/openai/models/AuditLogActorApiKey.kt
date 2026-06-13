// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The API Key used to perform the audit logged action.
 */
@Serializable
data class AuditLogActorApiKey(
    /**
     * The tracking id of the API key.
     */
    val id: String? = null,
    /**
     * The type of API key. Can be either `user` or `service_account`.
     */
    val type: String? = null,
    val user: site.addzero.api.openai.models.AuditLogActorUser? = null,
    @SerialName("service_account")
    val serviceAccount: site.addzero.api.openai.models.AuditLogActorServiceAccount? = null
)
