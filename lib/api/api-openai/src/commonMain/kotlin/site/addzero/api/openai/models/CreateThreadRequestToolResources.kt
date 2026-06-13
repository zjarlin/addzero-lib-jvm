// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A set of resources that are made available to the assistant's tools in this thread. The resources
 * are specific to the type of tool. For example, the `code_interpreter` tool requires a list of file
 * IDs, while the `file_search` tool requires a list of vector store IDs.
 */
@Serializable
data class CreateThreadRequestToolResources(
    @SerialName("code_interpreter")
    val codeInterpreter: site.addzero.api.openai.models.CreateThreadRequestToolResourcesCodeInterpreter? = null,
    @SerialName("file_search")
    val fileSearch: JsonElement? = null
)
