// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A StoredCompletionsRunDataSource configuration describing a set of filters
 */
@Serializable
data class EvalStoredCompletionsSource(
    /**
     * The type of source. Always `stored_completions`.
     */
    val type: String = "stored_completions",
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    val model: String? = null,
    @SerialName("created_after")
    val createdAfter: Int? = null,
    @SerialName("created_before")
    val createdBefore: Int? = null,
    val limit: Int? = null
)
