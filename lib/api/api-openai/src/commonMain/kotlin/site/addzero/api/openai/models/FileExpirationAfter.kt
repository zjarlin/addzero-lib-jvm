// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The expiration policy for a file. By default, files with `purpose=batch` expire after 30 days and
 * all other files are persisted until they are manually deleted.
 */
@Serializable
data class FileExpirationAfter(
    /**
     * Anchor timestamp after which the expiration policy applies. Supported anchors: `created_at`.
     */
    val anchor: String,
    /**
     * The number of seconds after the anchor time that the file will expire. Must be between 3600 (1 hour)
     * and 2592000 (30 days).
     */
    val seconds: Long
)
