// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a container created with /v1/containers.
 */
@Serializable
data class ContainerReferenceResource(
    /**
     * The environment type. Always `container_reference`.
     */
    val type: String = "container_reference",
    @SerialName("container_id")
    val containerId: String
)
