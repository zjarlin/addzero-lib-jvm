// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The user who performed the audit logged action.
 */
@Serializable
data class AuditLogActorUser(
    /**
     * The user id.
     */
    val id: String? = null,
    /**
     * The user email.
     */
    val email: String? = null
)
