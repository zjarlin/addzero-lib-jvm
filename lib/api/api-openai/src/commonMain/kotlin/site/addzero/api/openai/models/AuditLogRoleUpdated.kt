// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogRoleUpdated(
    /**
     * The role ID.
     */
    val id: String? = null,
    /**
     * The payload used to update the role.
     */
    @SerialName("changes_requested")
    val changesRequested: site.addzero.api.openai.models.AuditLogRoleUpdatedChangesRequested? = null
)
