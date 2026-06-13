// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteEvalResponse(
    @SerialName("object")
    val objectType: String,
    val deleted: Boolean,
    @SerialName("eval_id")
    val evalId: String
)
