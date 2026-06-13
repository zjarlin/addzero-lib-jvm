// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * PythonGrader
 */
@Serializable
data class EvalGraderPython(
    /**
     * The object type, which is always `python`.
     */
    val type: String,
    /**
     * The name of the grader.
     */
    val name: String,
    /**
     * The source code of the python script.
     */
    val source: String,
    /**
     * The image tag to use for the python script.
     */
    @SerialName("image_tag")
    val imageTag: String? = null,
    /**
     * The threshold for the score.
     */
    @SerialName("pass_threshold")
    val passThreshold: Double? = null
)
