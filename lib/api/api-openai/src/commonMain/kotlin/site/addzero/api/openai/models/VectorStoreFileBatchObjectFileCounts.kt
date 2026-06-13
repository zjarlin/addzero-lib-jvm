// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VectorStoreFileBatchObjectFileCounts(
    /**
     * The number of files that are currently being processed.
     */
    @SerialName("in_progress")
    val inProgress: Int,
    /**
     * The number of files that have been processed.
     */
    val completed: Int,
    /**
     * The number of files that have failed to process.
     */
    val failed: Int,
    /**
     * The number of files that where cancelled.
     */
    val cancelled: Int,
    /**
     * The total number of files.
     */
    val total: Int
)
