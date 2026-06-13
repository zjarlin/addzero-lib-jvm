// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RunGraderResponseMetadata(
    val name: String,
    val type: String,
    val errors: site.addzero.api.openai.models.RunGraderResponseMetadataErrors,
    @SerialName("execution_time")
    val executionTime: Double,
    val scores: Map<String, JsonElement>,
    @SerialName("token_usage")
    val tokenUsage: Int?,
    @SerialName("sampled_model_name")
    val sampledModelName: String?
)
