// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual certificate configured at the project level.
 */
@Serializable
data class OrganizationProjectCertificate(
    /**
     * The object type, which is always `organization.project.certificate`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The name of the certificate.
     */
    val name: String?,
    /**
     * The Unix timestamp (in seconds) of when the certificate was uploaded.
     */
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("certificate_details")
    val certificateDetails: site.addzero.api.openai.models.OrganizationProjectCertificateCertificateDetails,
    /**
     * Whether the certificate is currently active at the project level.
     */
    val active: Boolean
)
