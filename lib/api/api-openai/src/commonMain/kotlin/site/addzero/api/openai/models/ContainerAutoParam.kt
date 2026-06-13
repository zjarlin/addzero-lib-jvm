// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ContainerAutoParam(
    /**
     * Automatically creates a container for this request
     */
    val type: String = "container_auto",
    /**
     * An optional list of uploaded files to make available to your code.
     */
    @SerialName("file_ids")
    val fileIds: List<String>? = null,
    @SerialName("memory_limit")
    val memoryLimit: site.addzero.api.openai.models.ContainerMemoryLimit? = null,
    /**
     * Network access policy for the container.
     */
    @SerialName("network_policy")
    val networkPolicy: JsonElement? = null,
    /**
     * An optional list of skills referenced by id or inline data.
     */
    val skills: List<JsonElement>? = null
)
