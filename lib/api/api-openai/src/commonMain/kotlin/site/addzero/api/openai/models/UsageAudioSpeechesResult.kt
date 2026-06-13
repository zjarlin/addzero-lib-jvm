// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The aggregated audio speeches usage details of the specific time bucket.
 */
@Serializable
data class UsageAudioSpeechesResult(
    @SerialName("object")
    val objectType: String,
    /**
     * The number of characters processed.
     */
    val characters: Int,
    /**
     * The count of requests made to the model.
     */
    @SerialName("num_model_requests")
    val numModelRequests: Int,
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("api_key_id")
    val apiKeyId: String? = null,
    val model: String? = null
)
