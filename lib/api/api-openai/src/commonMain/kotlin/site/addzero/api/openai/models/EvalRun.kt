// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A schema representing an evaluation run.
 */
@Serializable
data class EvalRun(
    /**
     * The type of the object. Always "eval.run".
     */
    @SerialName("object")
    val objectType: String = "eval.run",
    /**
     * Unique identifier for the evaluation run.
     */
    val id: String,
    /**
     * The identifier of the associated evaluation.
     */
    @SerialName("eval_id")
    val evalId: String,
    /**
     * The status of the evaluation run.
     */
    val status: String,
    /**
     * The model that is evaluated, if applicable.
     */
    val model: String,
    /**
     * The name of the evaluation run.
     */
    val name: String,
    /**
     * Unix timestamp (in seconds) when the evaluation run was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The URL to the rendered evaluation run report on the UI dashboard.
     */
    @SerialName("report_url")
    val reportUrl: String,
    /**
     * Counters summarizing the outcomes of the evaluation run.
     */
    @SerialName("result_counts")
    val resultCounts: site.addzero.api.openai.models.EvalRunResultCounts,
    /**
     * Usage statistics for each model during the evaluation run.
     */
    @SerialName("per_model_usage")
    val perModelUsage: List<site.addzero.api.openai.models.EvalRunPerModelUsageItem>,
    /**
     * Results per testing criteria applied during the evaluation run.
     */
    @SerialName("per_testing_criteria_results")
    val perTestingCriteriaResults: List<site.addzero.api.openai.models.EvalRunPerTestingCriteriaResult>,
    /**
     * Information about the run's data source.
     */
    @SerialName("data_source")
    val dataSource: JsonElement,
    val metadata: site.addzero.api.openai.models.Metadata?,
    val error: site.addzero.api.openai.models.EvalApiError
)
