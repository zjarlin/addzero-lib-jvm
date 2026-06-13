// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent when a background response has failed.
 */
@Serializable
data class WebhookResponseFailed(
  /**
     * The Unix timestamp (in seconds) of when the model response failed.
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
    val data: site.addzero.api.openai.models.WebhookResponseFailedData,
  /**
     * The object of the event. Always `event`.
     */
    @SerialName("object")
    val objectType: String? = null,
  /**
     * The type of the event. Always `response.failed`.
     */
    val type: String
)
