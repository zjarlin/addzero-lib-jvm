// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogServiceAccountUpdated(
    /**
     * The service account ID.
     */
    val id: String? = null,
    /**
     * The payload used to updated the service account.
     */
    @SerialName("changes_requested")
    val changesRequested: site.addzero.api.openai.models.AuditLogServiceAccountUpdatedChangesRequested? = null
)
