// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The request counts for different statuses within the batch.
 */
@Serializable
data class BatchRequestCounts(
    /**
     * Total number of requests in the batch.
     */
    val total: Int,
    /**
     * Number of requests that have been completed successfully.
     */
    val completed: Int,
    /**
     * Number of requests that have failed.
     */
    val failed: Int
)
