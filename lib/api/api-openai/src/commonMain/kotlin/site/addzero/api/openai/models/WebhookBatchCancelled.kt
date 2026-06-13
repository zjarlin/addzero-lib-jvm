// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent when a batch API request has been cancelled.
 */
@Serializable
data class WebhookBatchCancelled(
    /**
     * The Unix timestamp (in seconds) of when the batch API request was cancelled.
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
    val data: site.addzero.api.openai.models.WebhookBatchCancelledData,
    /**
     * The object of the event. Always `event`.
     */
    @SerialName("object")
    val objectType: String? = null,
    /**
     * The type of the event. Always `batch.cancelled`.
     */
    val type: String
)
