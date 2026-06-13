// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Controls diagnostic tracing during the session.
 */
@Serializable
data class ChatkitWorkflowTracing(
    /**
     * Indicates whether tracing is enabled.
     */
    val enabled: Boolean
)
