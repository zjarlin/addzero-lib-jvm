// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogCertificatesDeactivated(
    val certificates: List<site.addzero.api.openai.models.AuditLogCertificatesDeactivatedCertificate>? = null
)
