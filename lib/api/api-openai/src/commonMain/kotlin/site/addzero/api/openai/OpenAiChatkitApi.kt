// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.ChatSessionResource
import site.addzero.api.openai.models.CreateChatSessionBody
import site.addzero.api.openai.models.DeletedThreadResource
import site.addzero.api.openai.models.OrderEnum
import site.addzero.api.openai.models.ThreadItemListResource
import site.addzero.api.openai.models.ThreadListResource
import site.addzero.api.openai.models.ThreadResource

interface OpenAiChatkitApi {

    /**
     * Cancel an active ChatKit session and return its most recent metadata. Cancelling prevents new
     * requests from using the issued client secret. REST: POST /chatkit/sessions/{session_id}/cancel
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHATKIT_BY_SESSIONS_BY_SESSION_ID_BY_CANCEL)
    suspend fun cancelChatSessionMethod(
        @Path("session_id") sessionId: String
    ): site.addzero.api.openai.models.ChatSessionResource

    /**
     * Create a ChatKit session. REST: POST /chatkit/sessions
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHATKIT_BY_SESSIONS)
    suspend fun createChatSessionMethod(
        @Body body: site.addzero.api.openai.models.CreateChatSessionBody? = null
    ): site.addzero.api.openai.models.ChatSessionResource

    /**
     * List items that belong to a ChatKit thread. REST: GET /chatkit/threads/{thread_id}/items
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHATKIT_BY_THREADS_BY_THREAD_ID_BY_ITEMS)
    suspend fun listThreadItemsMethod(
        @Path("thread_id") threadId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: site.addzero.api.openai.models.OrderEnum? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): site.addzero.api.openai.models.ThreadItemListResource

    /**
     * Retrieve a ChatKit thread by its identifier. REST: GET /chatkit/threads/{thread_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHATKIT_BY_THREADS_BY_THREAD_ID)
    suspend fun getThreadMethod(
        @Path("thread_id") threadId: String
    ): site.addzero.api.openai.models.ThreadResource

    /**
     * Delete a ChatKit thread along with its items and stored attachments. REST: DELETE
     * /chatkit/threads/{thread_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHATKIT_BY_THREADS_BY_THREAD_ID)
    suspend fun deleteThreadMethod(
        @Path("thread_id") threadId: String
    ): site.addzero.api.openai.models.DeletedThreadResource

    /**
     * List ChatKit threads with optional pagination and user filters. REST: GET /chatkit/threads
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.CHATKIT_BY_THREADS)
    suspend fun listThreadsMethod(
        @Query("limit") limit: Int? = null,
        @Query("order") order: site.addzero.api.openai.models.OrderEnum? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("user") user: String? = null
    ): site.addzero.api.openai.models.ThreadListResource

}
