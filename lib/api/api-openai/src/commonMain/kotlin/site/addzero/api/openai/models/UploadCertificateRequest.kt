// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class UploadCertificateRequest(
    /**
     * An optional name for the certificate
     */
    val name: String? = null,
    /**
     * The certificate content in PEM format
     */
    val certificate: String
)
