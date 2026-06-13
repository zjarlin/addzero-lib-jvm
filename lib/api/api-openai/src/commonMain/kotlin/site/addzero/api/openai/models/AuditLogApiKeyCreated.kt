// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogApiKeyCreated(
    /**
     * The tracking ID of the API key.
     */
    val id: String? = null,
    /**
     * The payload used to create the API key.
     */
    val data: site.addzero.api.openai.models.AuditLogApiKeyCreatedData? = null
)
