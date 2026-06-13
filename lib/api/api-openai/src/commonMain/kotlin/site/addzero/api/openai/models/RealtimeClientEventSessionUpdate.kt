// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Send this event to update the session’s configuration. The client may send this event at any time to
 * update any field except for `voice` and `model`. `voice` can be updated only if there have been no
 * other audio outputs yet. When the server receives a `session.update`, it will respond with a
 * `session.updated` event showing the full, effective configuration. Only the fields that are present
 * in the `session.update` are updated. To clear a field like `instructions`, pass an empty string. To
 * clear a field like `tools`, pass an empty array. To clear a field like `turn_detection`, pass
 * `null`.
 */
@Serializable
data class RealtimeClientEventSessionUpdate(
    /**
     * Optional client-generated ID used to identify this event. This is an arbitrary string that a client
     * may assign. It will be passed back if there is an error with the event, but the corresponding
     * `session.updated` event will not include it.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `session.update`.
     */
    val type: String,
    /**
     * Update the Realtime session. Choose either a realtime session or a transcription session.
     */
    val session: JsonElement
)
