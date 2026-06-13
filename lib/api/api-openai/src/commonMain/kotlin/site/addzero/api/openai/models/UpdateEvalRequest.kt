// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEvalRequest(
    /**
     * Rename the evaluation.
     */
    val name: String? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null
)
