// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateContainerBody(
    /**
     * Name of the container to create.
     */
    val name: String,
    /**
     * IDs of files to copy to the container.
     */
    @SerialName("file_ids")
    val fileIds: List<String>? = null,
    /**
     * Container expiration time in seconds relative to the 'anchor' time.
     */
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.CreateContainerBodyExpiresAfter? = null,
    /**
     * An optional list of skills referenced by id or inline data.
     */
    val skills: List<JsonElement>? = null,
    /**
     * Optional memory limit for the container. Defaults to "1g".
     */
    @SerialName("memory_limit")
    val memoryLimit: String? = null,
    /**
     * Network access policy for the container.
     */
    @SerialName("network_policy")
    val networkPolicy: JsonElement? = null
)
