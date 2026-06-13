// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A MultiGrader object combines the output of multiple graders to produce a single score.
 */
@Serializable
data class GraderMulti(
    /**
     * The object type, which is always `multi`.
     */
    val type: String = "multi",
    /**
     * The name of the grader.
     */
    val name: String,
    val graders: JsonElement,
    /**
     * A formula to calculate the output based on grader results.
     */
    @SerialName("calculate_output")
    val calculateOutput: String
)
