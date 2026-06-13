// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContainerReferenceParam(
    /**
     * References a container created with the /v1/containers endpoint
     */
    val type: String = "container_reference",
    /**
     * The ID of the referenced container.
     */
    @SerialName("container_id")
    val containerId: String
)
