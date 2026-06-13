// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The actor who performed the audit logged action.
 */
@Serializable
data class AuditLogActor(
    /**
     * The type of actor. Is either `session` or `api_key`.
     */
    val type: String? = null,
    val session: site.addzero.api.openai.models.AuditLogActorSession? = null,
    @SerialName("api_key")
    val apiKey: site.addzero.api.openai.models.AuditLogActorApiKey? = null
)
