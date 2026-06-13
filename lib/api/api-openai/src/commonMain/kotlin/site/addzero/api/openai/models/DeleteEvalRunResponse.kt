// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteEvalRunResponse(
    @SerialName("object")
    val objectType: String? = null,
    val deleted: Boolean? = null,
    @SerialName("run_id")
    val runId: String? = null
)
