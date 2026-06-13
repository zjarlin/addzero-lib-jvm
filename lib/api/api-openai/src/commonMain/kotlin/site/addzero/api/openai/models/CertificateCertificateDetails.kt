// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CertificateCertificateDetails(
    /**
     * The Unix timestamp (in seconds) of when the certificate becomes valid.
     */
    @SerialName("valid_at")
    val validAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) of when the certificate expires.
     */
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    /**
     * The content of the certificate in PEM format.
     */
    val content: String? = null
)
