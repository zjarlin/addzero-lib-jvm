// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The output of a code interpreter tool call that is a file.
 */
@Serializable
data class CodeInterpreterFileOutput(
    /**
     * The type of the code interpreter file output. Always `files`.
     */
    val type: String,
    val files: List<site.addzero.api.openai.models.CodeInterpreterFileOutputFile>
)
