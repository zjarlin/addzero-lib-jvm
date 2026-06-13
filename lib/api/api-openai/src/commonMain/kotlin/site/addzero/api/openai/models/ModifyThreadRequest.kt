// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModifyThreadRequest(
    @SerialName("tool_resources")
    val toolResources: site.addzero.api.openai.models.ModifyThreadRequestToolResources? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null
)
