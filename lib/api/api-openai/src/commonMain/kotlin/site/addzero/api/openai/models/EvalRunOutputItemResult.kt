// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A single grader result for an evaluation run output item.
 */
@Serializable
data class EvalRunOutputItemResult(
    /**
     * The name of the grader.
     */
    val name: String,
    /**
     * The grader type (for example, "string-check-grader").
     */
    val type: String? = null,
    /**
     * The numeric score produced by the grader.
     */
    val score: Double,
    /**
     * Whether the grader considered the output a pass.
     */
    val passed: Boolean,
    /**
     * Optional sample or intermediate data produced by the grader.
     */
    val sample: Map<String, JsonElement>? = null
)
