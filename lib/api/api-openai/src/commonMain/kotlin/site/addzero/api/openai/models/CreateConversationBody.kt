// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationBody(
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    val items: List<site.addzero.api.openai.models.InputItem>? = null
)
