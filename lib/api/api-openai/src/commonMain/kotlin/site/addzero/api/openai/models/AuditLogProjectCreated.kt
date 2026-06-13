// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogProjectCreated(
    /**
     * The project ID.
     */
    val id: String? = null,
    /**
     * The payload used to create the project.
     */
    val data: site.addzero.api.openai.models.AuditLogProjectCreatedData? = null
)
