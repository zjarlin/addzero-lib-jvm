// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogGroupCreated(
    /**
     * The ID of the group.
     */
    val id: String? = null,
    /**
     * Information about the created group.
     */
    val data: site.addzero.api.openai.models.AuditLogGroupCreatedData? = null
)
