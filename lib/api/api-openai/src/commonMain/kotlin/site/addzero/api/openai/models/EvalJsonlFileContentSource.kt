// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * EvalJsonlFileContentSource
 */
@Serializable
data class EvalJsonlFileContentSource(
    /**
     * The type of jsonl source. Always `file_content`.
     */
    val type: String = "file_content",
    /**
     * The content of the jsonl file.
     */
    val content: List<site.addzero.api.openai.models.EvalJsonlFileContentSourceContentItem>
)
