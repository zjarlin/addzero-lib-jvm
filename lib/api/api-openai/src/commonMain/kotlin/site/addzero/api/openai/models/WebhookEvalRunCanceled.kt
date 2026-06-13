// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent when an eval run has been canceled.
 */
@Serializable
data class WebhookEvalRunCanceled(
  /**
     * The Unix timestamp (in seconds) of when the eval run was canceled.
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
    val data: site.addzero.api.openai.models.WebhookEvalRunCanceledData,
  /**
     * The object of the event. Always `event`.
     */
    @SerialName("object")
    val objectType: String? = null,
  /**
     * The type of the event. Always `eval.run.canceled`.
     */
    val type: String
)
