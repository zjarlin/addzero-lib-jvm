// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteCertificateResponse(
    /**
     * The object type, must be `certificate.deleted`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The ID of the certificate that was deleted.
     */
    val id: String
)
