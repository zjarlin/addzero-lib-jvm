// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogUserAdded(
    /**
     * The user ID.
     */
    val id: String? = null,
    /**
     * The payload used to add the user to the project.
     */
    val data: site.addzero.api.openai.models.AuditLogUserAddedData? = null
)
