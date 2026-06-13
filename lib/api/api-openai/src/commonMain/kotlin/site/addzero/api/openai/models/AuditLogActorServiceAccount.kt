// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The service account that performed the audit logged action.
 */
@Serializable
data class AuditLogActorServiceAccount(
    /**
     * The service account id.
     */
    val id: String? = null
)
