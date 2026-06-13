// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The expiration policy for a vector store.
 */
@Serializable
data class VectorStoreExpirationAfter(
    /**
     * Anchor timestamp after which the expiration policy applies. Supported anchors: `last_active_at`.
     */
    val anchor: String,
    /**
     * The number of days after the anchor time that the vector store will expire.
     */
    val days: Int
)
