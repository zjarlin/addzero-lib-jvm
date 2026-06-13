// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The session in which the audit logged action was performed.
 */
@Serializable
data class AuditLogActorSession(
    val user: site.addzero.api.openai.models.AuditLogActorUser? = null,
    /**
     * The IP address from which the action was performed.
     */
    @SerialName("ip_address")
    val ipAddress: String? = null
)
