// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Send this event to cancel an in-progress response. The server will respond with a `response.done`
 * event with a status of `response.status=cancelled`. If there is no response to cancel, the server
 * will respond with an error. It's safe to call `response.cancel` even if no response is in progress,
 * an error will be returned the session will remain unaffected.
 */
@Serializable
data class RealtimeClientEventResponseCancel(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `response.cancel`.
     */
    val type: String,
    /**
     * A specific response ID to cancel - if not provided, will cancel an in-progress response in the
     * default conversation.
     */
    @SerialName("response_id")
    val responseId: String? = null
)
