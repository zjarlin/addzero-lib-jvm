// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual `certificate` uploaded to the organization.
 */
@Serializable
data class Certificate(
    /**
     * The object type. - If creating, updating, or getting a specific certificate, the object type is
     * `certificate`. - If listing, activating, or deactivating certificates for the organization, the
     * object type is `organization.certificate`. - If listing, activating, or deactivating certificates
     * for a project, the object type is `organization.project.certificate`.
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
    val certificateDetails: site.addzero.api.openai.models.CertificateCertificateDetails,
    /**
     * Whether the certificate is currently active at the specified scope. Not returned when getting
     * details for a specific certificate.
     */
    val active: Boolean? = null
)
