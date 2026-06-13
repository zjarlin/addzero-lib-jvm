// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent when a fine-tuning job has been cancelled.
 */
@Serializable
data class WebhookFineTuningJobCancelled(
  /**
     * The Unix timestamp (in seconds) of when the fine-tuning job was cancelled.
     */
    @SerialName("created_at")
    val createdAt: Long,
  /**
     * The unique ID of the event.
     */
    val id: String,
  /**
     * Event data payload.
     */
    val data: site.addzero.api.openai.models.WebhookFineTuningJobCancelledData,
  /**
     * The object of the event. Always `event`.
     */
    @SerialName("object")
    val objectType: String? = null,
  /**
     * The type of the event. Always `fine_tuning.job.cancelled`.
     */
    val type: String
)
