// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Controls how the audio is cut into chunks. When set to `"auto"`, the server first normalizes
 * loudness and then uses voice activity detection (VAD) to choose boundaries. `server_vad` object can
 * be provided to tweak VAD detection parameters manually. If unset, the audio is transcribed as a
 * single block.
 */
@Serializable
data class TranscriptionChunkingStrategy(
    val value: Map<String, JsonElement> = emptyMap()
)
