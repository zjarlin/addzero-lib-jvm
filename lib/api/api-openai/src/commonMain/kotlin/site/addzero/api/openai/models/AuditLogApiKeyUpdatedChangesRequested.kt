// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The payload used to update the API key.
 */
@Serializable
data class AuditLogApiKeyUpdatedChangesRequested(
    /**
     * A list of scopes allowed for the API key, e.g. `["api.model.request"]`
     */
    val scopes: List<String>? = null
)
