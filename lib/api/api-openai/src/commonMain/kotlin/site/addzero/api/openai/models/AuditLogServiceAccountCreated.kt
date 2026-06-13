// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogServiceAccountCreated(
    /**
     * The service account ID.
     */
    val id: String? = null,
    /**
     * The payload used to create the service account.
     */
    val data: site.addzero.api.openai.models.AuditLogServiceAccountCreatedData? = null
)
