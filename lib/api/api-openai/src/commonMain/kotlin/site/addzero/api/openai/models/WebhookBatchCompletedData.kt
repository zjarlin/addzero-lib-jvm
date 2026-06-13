// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Event data payload.
 */
@Serializable
data class WebhookBatchCompletedData(
    /**
     * The unique ID of the batch API request.
     */
    val id: String
)
