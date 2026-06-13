// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when a [run](/docs/api-reference/runs/object) is completed.
 */
@Serializable
data class RunStreamEvent6(
    val event: String,
    val data: site.addzero.api.openai.models.RunObject
)
