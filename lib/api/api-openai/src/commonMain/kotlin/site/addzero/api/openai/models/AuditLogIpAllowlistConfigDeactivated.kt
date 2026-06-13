// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogIpAllowlistConfigDeactivated(
    /**
     * The configurations that were deactivated.
     */
    val configs: List<site.addzero.api.openai.models.AuditLogIpAllowlistConfigDeactivatedConfig>? = null
)
