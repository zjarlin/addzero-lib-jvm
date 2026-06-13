// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToggleCertificatesRequest(
    @SerialName("certificate_ids")
    val certificateIds: List<String>
)
