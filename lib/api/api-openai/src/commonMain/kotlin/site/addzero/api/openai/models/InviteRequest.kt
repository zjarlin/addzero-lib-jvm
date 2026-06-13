// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class InviteRequest(
    /**
     * Send an email to this address
     */
    val email: String,
    /**
     * `owner` or `reader`
     */
    val role: String,
    /**
     * An array of projects to which membership is granted at the same time the org invite is accepted. If
     * omitted, the user will be invited to the default project for compatibility with legacy behavior.
     */
    val projects: List<site.addzero.api.openai.models.InviteRequestProject>? = null
)
