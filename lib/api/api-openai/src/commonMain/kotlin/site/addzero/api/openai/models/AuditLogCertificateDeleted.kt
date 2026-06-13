// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogCertificateDeleted(
    /**
     * The certificate ID.
     */
    val id: String? = null,
    /**
     * The name of the certificate.
     */
    val name: String? = null,
    /**
     * The certificate content in PEM format.
     */
    val certificate: String? = null
)
