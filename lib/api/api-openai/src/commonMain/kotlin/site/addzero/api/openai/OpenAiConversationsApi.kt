// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.ConversationItem
import site.addzero.api.openai.models.ConversationItemList
import site.addzero.api.openai.models.ConversationResource
import site.addzero.api.openai.models.CreateConversationBody
import site.addzero.api.openai.models.CreateConversationItemsRequest
import site.addzero.api.openai.models.DeletedConversationResource
import site.addzero.api.openai.models.IncludeEnum
import site.addzero.api.openai.models.UpdateConversationBody

interface OpenAiConversationsApi {

    /**
     * List all items for a conversation with the given ID. REST: GET
     * /conversations/{conversation_id}/items
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID_BY_ITEMS)
    suspend fun listConversationItems(
        @Path("conversation_id") conversationId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("include") include: List<site.addzero.api.openai.models.IncludeEnum>? = null
    ): site.addzero.api.openai.models.ConversationItemList

    /**
     * Create items in a conversation with the given ID. REST: POST /conversations/{conversation_id}/items
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID_BY_ITEMS)
    suspend fun createConversationItems(
        @Path("conversation_id") conversationId: String,
        @Query("include") include: List<site.addzero.api.openai.models.IncludeEnum>? = null,
        @Body body: site.addzero.api.openai.models.CreateConversationItemsRequest
    ): site.addzero.api.openai.models.ConversationItemList

    /**
     * Get a single item from a conversation with the given IDs. REST: GET
     * /conversations/{conversation_id}/items/{item_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID_BY_ITEMS_BY_ITEM_ID)
    suspend fun getConversationItem(
        @Path("conversation_id") conversationId: String,
        @Path("item_id") itemId: String,
        @Query("include") include: List<site.addzero.api.openai.models.IncludeEnum>? = null
    ): site.addzero.api.openai.models.ConversationItem

    /**
     * Delete an item from a conversation with the given IDs. REST: DELETE
     * /conversations/{conversation_id}/items/{item_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID_BY_ITEMS_BY_ITEM_ID)
    suspend fun deleteConversationItem(
        @Path("conversation_id") conversationId: String,
        @Path("item_id") itemId: String
    ): site.addzero.api.openai.models.ConversationResource

    /**
     * Create a conversation. REST: POST /conversations
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONVERSATIONS)
    suspend fun createConversation(
        @Body body: site.addzero.api.openai.models.CreateConversationBody? = null
    ): site.addzero.api.openai.models.ConversationResource

    /**
     * Get a conversation REST: GET /conversations/{conversation_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID)
    suspend fun getConversation(
        @Path("conversation_id") conversationId: String
    ): site.addzero.api.openai.models.ConversationResource

    /**
     * Update a conversation REST: POST /conversations/{conversation_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID)
    suspend fun updateConversation(
        @Path("conversation_id") conversationId: String,
        @Body body: site.addzero.api.openai.models.UpdateConversationBody? = null
    ): site.addzero.api.openai.models.ConversationResource

    /**
     * Delete a conversation. Items in the conversation will not be deleted. REST: DELETE
     * /conversations/{conversation_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CONVERSATIONS_BY_CONVERSATION_ID)
    suspend fun deleteConversation(
        @Path("conversation_id") conversationId: String
    ): site.addzero.api.openai.models.DeletedConversationResource

}
