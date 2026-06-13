// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UsageTimeBucket(
    @SerialName("object")
    val objectType: String,
    @SerialName("start_time")
    val startTime: Int,
    @SerialName("end_time")
    val endTime: Int,
    val results: List<JsonElement>
)
