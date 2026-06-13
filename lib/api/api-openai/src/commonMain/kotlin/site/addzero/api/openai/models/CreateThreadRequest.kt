// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Options to create a new thread. If no thread is provided when running a request, an empty thread
 * will be created.
 */
@Serializable
data class CreateThreadRequest(
    /**
     * A list of [messages](/docs/api-reference/messages) to start the thread with.
     */
    val messages: List<site.addzero.api.openai.models.CreateMessageRequest>? = null,
    @SerialName("tool_resources")
    val toolResources: site.addzero.api.openai.models.CreateThreadRequestToolResources? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null
)
