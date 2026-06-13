// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Chatkit REST endpoints. */
interface OpenAiChatkitApi {

    /**
     * Create a ChatKit session.
     *
     * REST: POST /chatkit/sessions
     */
    @POST(OpenAiApiPaths.CHATKIT_BY_SESSIONS)
    suspend fun createChatSessionMethod(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Cancel an active ChatKit session and return its most recent metadata. Cancelling prevents new requests from using the issued client secret.
     *
     * REST: POST /chatkit/sessions/{session_id}/cancel
     */
    @POST(OpenAiApiPaths.CHATKIT_BY_SESSIONS_BY_SESSION_ID_BY_CANCEL)
    suspend fun cancelChatSessionMethod(
        @Path("session_id") sessionId: String
    ): OpenAiResponseBody

    /**
     * List ChatKit threads with optional pagination and user filters.
     *
     * REST: GET /chatkit/threads
     */
    @GET(OpenAiApiPaths.CHATKIT_BY_THREADS)
    suspend fun listThreadsMethod(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("user") user: String? = null
    ): OpenAiResponseBody

    /**
     * Delete a ChatKit thread along with its items and stored attachments.
     *
     * REST: DELETE /chatkit/threads/{thread_id}
     */
    @DELETE(OpenAiApiPaths.CHATKIT_BY_THREADS_BY_THREAD_ID)
    suspend fun deleteThreadMethod(
        @Path("thread_id") threadId: String
    ): OpenAiResponseBody

    /**
     * Retrieve a ChatKit thread by its identifier.
     *
     * REST: GET /chatkit/threads/{thread_id}
     */
    @GET(OpenAiApiPaths.CHATKIT_BY_THREADS_BY_THREAD_ID)
    suspend fun getThreadMethod(
        @Path("thread_id") threadId: String
    ): OpenAiResponseBody

    /**
     * List items that belong to a ChatKit thread.
     *
     * REST: GET /chatkit/threads/{thread_id}/items
     */
    @GET(OpenAiApiPaths.CHATKIT_BY_THREADS_BY_THREAD_ID_BY_ITEMS)
    suspend fun listThreadItemsMethod(
        @Path("thread_id") threadId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): OpenAiResponseBody
}
