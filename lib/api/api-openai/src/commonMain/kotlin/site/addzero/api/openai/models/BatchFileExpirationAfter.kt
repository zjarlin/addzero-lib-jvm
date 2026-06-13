// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The expiration policy for the output and/or error file that are generated for a batch.
 */
@Serializable
data class BatchFileExpirationAfter(
    /**
     * Anchor timestamp after which the expiration policy applies. Supported anchors: `created_at`. Note
     * that the anchor is the file creation time, not the time the batch is created.
     */
    val anchor: String,
    /**
     * The number of seconds after the anchor time that the file will expire. Must be between 3600 (1 hour)
     * and 2592000 (30 days).
     */
    val seconds: Long
)
