// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogIpAllowlistUpdated(
    /**
     * The ID of the IP allowlist configuration.
     */
    val id: String? = null,
    /**
     * The updated set of IP addresses or CIDR ranges in the configuration.
     */
    @SerialName("allowed_ips")
    val allowedIps: List<String>? = null
)
