// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Controls whether ChatKit automatically generates thread titles.
 */
@Serializable
data class AutomaticThreadTitlingParam(
    /**
     * Enable automatic thread title generation. Defaults to true.
     */
    val enabled: Boolean? = null
)
