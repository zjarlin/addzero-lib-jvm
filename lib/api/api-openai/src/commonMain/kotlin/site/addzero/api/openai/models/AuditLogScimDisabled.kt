// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogScimDisabled(
    /**
     * The ID of the SCIM was disabled for.
     */
    val id: String? = null
)
