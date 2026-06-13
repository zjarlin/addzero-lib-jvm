// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Represents the use of a local environment to perform shell actions.
 */
@Serializable
data class LocalEnvironmentResource(
    /**
     * The environment type. Always `local`.
     */
    val type: String = "local"
)
