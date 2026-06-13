// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A schema representing an evaluation run output item.
 */
@Serializable
data class EvalRunOutputItem(
    /**
     * The type of the object. Always "eval.run.output_item".
     */
    @SerialName("object")
    val objectType: String = "eval.run.output_item",
    /**
     * Unique identifier for the evaluation run output item.
     */
    val id: String,
    /**
     * The identifier of the evaluation run associated with this output item.
     */
    @SerialName("run_id")
    val runId: String,
    /**
     * The identifier of the evaluation group.
     */
    @SerialName("eval_id")
    val evalId: String,
    /**
     * Unix timestamp (in seconds) when the evaluation run was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The status of the evaluation run.
     */
    val status: String,
    /**
     * The identifier for the data source item.
     */
    @SerialName("datasource_item_id")
    val datasourceItemId: Int,
    /**
     * Details of the input data source item.
     */
    @SerialName("datasource_item")
    val datasourceItem: Map<String, JsonElement>,
    /**
     * A list of grader results for this output item.
     */
    val results: List<site.addzero.api.openai.models.EvalRunOutputItemResult>,
    /**
     * A sample containing the input and output of the evaluation run.
     */
    val sample: site.addzero.api.openai.models.EvalRunOutputItemSample
)
