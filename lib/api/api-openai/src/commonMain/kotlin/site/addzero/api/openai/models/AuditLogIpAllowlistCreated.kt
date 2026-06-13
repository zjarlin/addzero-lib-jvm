// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogIpAllowlistCreated(
    /**
     * The ID of the IP allowlist configuration.
     */
    val id: String? = null,
    /**
     * The name of the IP allowlist configuration.
     */
    val name: String? = null,
    /**
     * The IP addresses or CIDR ranges included in the configuration.
     */
    @SerialName("allowed_ips")
    val allowedIps: List<String>? = null
)
