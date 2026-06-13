// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RunGraderResponse(
    val reward: Double,
    val metadata: site.addzero.api.openai.models.RunGraderResponseMetadata,
    @SerialName("sub_rewards")
    val subRewards: Map<String, JsonElement>,
    @SerialName("model_grader_token_usage_per_model")
    val modelGraderTokenUsagePerModel: Map<String, JsonElement>
)
