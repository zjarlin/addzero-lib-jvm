// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadObjectToolResourcesCodeInterpreter(
    /**
     * A list of [file](/docs/api-reference/files) IDs made available to the `code_interpreter` tool. There
     * can be a maximum of 20 files associated with the tool.
     */
    @SerialName("file_ids")
    val fileIds: List<String>? = null
)
