// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Fine-tuning job event object
 */
@Serializable
data class FineTuningJobEvent(
    /**
     * The object type, which is always "fine_tuning.job.event".
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The object identifier.
     */
    val id: String,
    /**
     * The Unix timestamp (in seconds) for when the fine-tuning job was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The log level of the event.
     */
    val level: String,
    /**
     * The message of the event.
     */
    val message: String,
    /**
     * The type of event.
     */
    val type: String? = null,
    /**
     * The data associated with the event.
     */
    val data: Map<String, JsonElement>? = null
)
