// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * EvalJsonlFileIdSource
 */
@Serializable
data class EvalJsonlFileIdSource(
    /**
     * The type of jsonl source. Always `file_id`.
     */
    val type: String = "file_id",
    /**
     * The identifier of the file.
     */
    val id: String
)
