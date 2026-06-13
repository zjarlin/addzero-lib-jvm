// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Counters summarizing the outcomes of the evaluation run.
 */
@Serializable
data class EvalRunResultCounts(
    /**
     * Total number of executed output items.
     */
    val total: Int,
    /**
     * Number of output items that resulted in an error.
     */
    val errored: Int,
    /**
     * Number of output items that failed to pass the evaluation.
     */
    val failed: Int,
    /**
     * Number of output items that passed the evaluation.
     */
    val passed: Int
)
