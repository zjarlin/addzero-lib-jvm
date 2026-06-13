// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * CreateEvalRunRequest
 */
@Serializable
data class CreateEvalRunRequest(
    /**
     * The name of the run.
     */
    val name: String? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    /**
     * Details about the run's data source.
     */
    @SerialName("data_source")
    val dataSource: JsonElement
)
