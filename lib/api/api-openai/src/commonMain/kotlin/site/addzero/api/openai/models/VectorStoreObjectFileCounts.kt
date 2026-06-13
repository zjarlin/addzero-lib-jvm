// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VectorStoreObjectFileCounts(
    /**
     * The number of files that are currently being processed.
     */
    @SerialName("in_progress")
    val inProgress: Int,
    /**
     * The number of files that have been successfully processed.
     */
    val completed: Int,
    /**
     * The number of files that have failed to process.
     */
    val failed: Int,
    /**
     * The number of files that were cancelled.
     */
    val cancelled: Int,
    /**
     * The total number of files.
     */
    val total: Int
)
