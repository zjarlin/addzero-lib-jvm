// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Conversations REST endpoints. */
interface OpenAiConversationsApi {

    /**
     * Create a conversation.
     *
     * REST: POST /conversations
     */
    @POST(OpenAiApiPaths.CONVERSATIONS)
    suspend fun createConversation(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Delete a conversation. Items in the conversation will not be deleted.
     *
     * REST: DELETE /conversations/{conversation_id}
     */
    @DELETE(OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID)
    suspend fun deleteConversation(
        @Path("conversation_id") conversationId: String
    ): OpenAiResponseBody

    /**
     * Get a conversation
     *
     * REST: GET /conversations/{conversation_id}
     */
    @GET(OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID)
    suspend fun getConversation(
        @Path("conversation_id") conversationId: String
    ): OpenAiResponseBody

    /**
     * Update a conversation
     *
     * REST: POST /conversations/{conversation_id}
     */
    @POST(OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID)
    suspend fun updateConversation(
        @Path("conversation_id") conversationId: String,
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * List all items for a conversation with the given ID.
     *
     * REST: GET /conversations/{conversation_id}/items
     */
    @GET(OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID_BY_ITEMS)
    suspend fun listConversationItems(
        @Path("conversation_id") conversationId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("include") include: List<String>? = null
    ): OpenAiResponseBody

    /**
     * Create items in a conversation with the given ID.
     *
     * REST: POST /conversations/{conversation_id}/items
     */
    @POST(OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID_BY_ITEMS)
    suspend fun createConversationItems(
        @Path("conversation_id") conversationId: String,
        @Query("include") include: List<String>? = null,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete an item from a conversation with the given IDs.
     *
     * REST: DELETE /conversations/{conversation_id}/items/{item_id}
     */
    @DELETE(OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID_BY_ITEMS_BY_ITEM_ID)
    suspend fun deleteConversationItem(
        @Path("conversation_id") conversationId: String,
        @Path("item_id") itemId: String
    ): OpenAiResponseBody

    /**
     * Get a single item from a conversation with the given IDs.
     *
     * REST: GET /conversations/{conversation_id}/items/{item_id}
     */
    @GET(OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID_BY_ITEMS_BY_ITEM_ID)
    suspend fun getConversationItem(
        @Path("conversation_id") conversationId: String,
        @Path("item_id") itemId: String,
        @Query("include") include: List<String>? = null
    ): OpenAiResponseBody
}
