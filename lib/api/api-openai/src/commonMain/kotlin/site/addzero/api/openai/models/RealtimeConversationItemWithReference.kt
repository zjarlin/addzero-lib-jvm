// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The item to add to the conversation.
 */
@Serializable
data class RealtimeConversationItemWithReference(
    /**
     * For an item of type (`message` | `function_call` | `function_call_output`) this field allows the
     * client to assign the unique ID of the item. It is not required because the server will generate one
     * if not provided. For an item of type `item_reference`, this field is required and is a reference to
     * any item that has previously existed in the conversation.
     */
    val id: String? = null,
    /**
     * The type of the item (`message`, `function_call`, `function_call_output`, `item_reference`).
     */
    val type: String? = null,
    /**
     * Identifier for the API object being returned - always `realtime.item`.
     */
    @SerialName("object")
    val objectType: String? = null,
    /**
     * The status of the item (`completed`, `incomplete`, `in_progress`). These have no effect on the
     * conversation, but are accepted for consistency with the `conversation.item.created` event.
     */
    val status: String? = null,
    /**
     * The role of the message sender (`user`, `assistant`, `system`), only applicable for `message` items.
     */
    val role: String? = null,
    /**
     * The content of the message, applicable for `message` items. - Message items of role `system` support
     * only `input_text` content - Message items of role `user` support `input_text` and `input_audio`
     * content - Message items of role `assistant` support `text` content.
     */
    val content: List<site.addzero.api.openai.models.RealtimeConversationItemWithReferenceContentItem>? = null,
    /**
     * The ID of the function call (for `function_call` and `function_call_output` items). If passed on a
     * `function_call_output` item, the server will check that a `function_call` item with the same ID
     * exists in the conversation history.
     */
    @SerialName("call_id")
    val callId: String? = null,
    /**
     * The name of the function being called (for `function_call` items).
     */
    val name: String? = null,
    /**
     * The arguments of the function call (for `function_call` items).
     */
    val arguments: String? = null,
    /**
     * The output of the function call (for `function_call_output` items).
     */
    val output: String? = null
)
