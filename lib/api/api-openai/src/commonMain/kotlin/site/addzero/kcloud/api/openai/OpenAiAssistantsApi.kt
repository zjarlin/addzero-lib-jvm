// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Assistants REST endpoints. */
interface OpenAiAssistantsApi {

    /**
     * Returns a list of assistants.
     *
     * REST: GET /assistants
     */
    @Deprecated("Deprecated by the OpenAI OpenAPI specification.")
    @GET(OpenAiApiPaths.ASSISTANTS)
    suspend fun listAssistants(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): OpenAiResponseBody

    /**
     * Create an assistant with a model and instructions.
     *
     * REST: POST /assistants
     */
    @Deprecated("Deprecated by the OpenAI OpenAPI specification.")
    @POST(OpenAiApiPaths.ASSISTANTS)
    suspend fun createAssistant(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete an assistant.
     *
     * REST: DELETE /assistants/{assistant_id}
     */
    @Deprecated("Deprecated by the OpenAI OpenAPI specification.")
    @DELETE(OpenAiApiPaths.ASSISTANTS_BY_ASSISTANT_ID)
    suspend fun deleteAssistant(
        @Path("assistant_id") assistantId: String
    ): OpenAiResponseBody

    /**
     * Retrieves an assistant.
     *
     * REST: GET /assistants/{assistant_id}
     */
    @Deprecated("Deprecated by the OpenAI OpenAPI specification.")
    @GET(OpenAiApiPaths.ASSISTANTS_BY_ASSISTANT_ID)
    suspend fun getAssistant(
        @Path("assistant_id") assistantId: String
    ): OpenAiResponseBody

    /**
     * Modifies an assistant.
     *
     * REST: POST /assistants/{assistant_id}
     */
    @Deprecated("Deprecated by the OpenAI OpenAPI specification.")
    @POST(OpenAiApiPaths.ASSISTANTS_BY_ASSISTANT_ID)
    suspend fun modifyAssistant(
        @Path("assistant_id") assistantId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Create a thread.
     *
     * REST: POST /threads
     */
    @POST(OpenAiApiPaths.THREADS)
    suspend fun createThread(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Create a thread and run it in one request.
     *
     * REST: POST /threads/runs
     */
    @POST(OpenAiApiPaths.THREADS_BY_RUNS)
    suspend fun createThreadAndRun(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete a thread.
     *
     * REST: DELETE /threads/{thread_id}
     */
    @DELETE(OpenAiApiPaths.THREADS_BY_THREAD_ID)
    suspend fun deleteThread(
        @Path("thread_id") threadId: String
    ): OpenAiResponseBody

    /**
     * Retrieves a thread.
     *
     * REST: GET /threads/{thread_id}
     */
    @GET(OpenAiApiPaths.THREADS_BY_THREAD_ID)
    suspend fun getThread(
        @Path("thread_id") threadId: String
    ): OpenAiResponseBody

    /**
     * Modifies a thread.
     *
     * REST: POST /threads/{thread_id}
     */
    @POST(OpenAiApiPaths.THREADS_BY_THREAD_ID)
    suspend fun modifyThread(
        @Path("thread_id") threadId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Returns a list of messages for a given thread.
     *
     * REST: GET /threads/{thread_id}/messages
     */
    @GET(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES)
    suspend fun listMessages(
        @Path("thread_id") threadId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("run_id") runId: String? = null
    ): OpenAiResponseBody

    /**
     * Create a message.
     *
     * REST: POST /threads/{thread_id}/messages
     */
    @POST(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES)
    suspend fun createMessage(
        @Path("thread_id") threadId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deletes a message.
     *
     * REST: DELETE /threads/{thread_id}/messages/{message_id}
     */
    @DELETE(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES_BY_MESSAGE_ID)
    suspend fun deleteMessage(
        @Path("thread_id") threadId: String,
        @Path("message_id") messageId: String
    ): OpenAiResponseBody

    /**
     * Retrieve a message.
     *
     * REST: GET /threads/{thread_id}/messages/{message_id}
     */
    @GET(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES_BY_MESSAGE_ID)
    suspend fun getMessage(
        @Path("thread_id") threadId: String,
        @Path("message_id") messageId: String
    ): OpenAiResponseBody

    /**
     * Modifies a message.
     *
     * REST: POST /threads/{thread_id}/messages/{message_id}
     */
    @POST(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES_BY_MESSAGE_ID)
    suspend fun modifyMessage(
        @Path("thread_id") threadId: String,
        @Path("message_id") messageId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Returns a list of runs belonging to a thread.
     *
     * REST: GET /threads/{thread_id}/runs
     */
    @GET(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS)
    suspend fun listRuns(
        @Path("thread_id") threadId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): OpenAiResponseBody

    /**
     * Create a run.
     *
     * REST: POST /threads/{thread_id}/runs
     */
    @POST(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS)
    suspend fun createRun(
        @Path("thread_id") threadId: String,
        @Query("include[]") include: List<String>? = null,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Retrieves a run.
     *
     * REST: GET /threads/{thread_id}/runs/{run_id}
     */
    @GET(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID)
    suspend fun getRun(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String
    ): OpenAiResponseBody

    /**
     * Modifies a run.
     *
     * REST: POST /threads/{thread_id}/runs/{run_id}
     */
    @POST(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID)
    suspend fun modifyRun(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Cancels a run that is `in_progress`.
     *
     * REST: POST /threads/{thread_id}/runs/{run_id}/cancel
     */
    @POST(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID_BY_CANCEL)
    suspend fun cancelRun(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String
    ): OpenAiResponseBody

    /**
     * Returns a list of run steps belonging to a run.
     *
     * REST: GET /threads/{thread_id}/runs/{run_id}/steps
     */
    @GET(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID_BY_STEPS)
    suspend fun listRunSteps(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("include[]") include: List<String>? = null
    ): OpenAiResponseBody

    /**
     * Retrieves a run step.
     *
     * REST: GET /threads/{thread_id}/runs/{run_id}/steps/{step_id}
     */
    @GET(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID_BY_STEPS_BY_STEP_ID)
    suspend fun getRunStep(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Path("step_id") stepId: String,
        @Query("include[]") include: List<String>? = null
    ): OpenAiResponseBody

    /**
     * When a run has the `status: "requires_action"` and `required_action.type` is `submit_tool_outputs`, this endpoint can be used to submit the outputs from the tool calls once they're all completed. All outputs must be submitted in a single request.
     *
     * REST: POST /threads/{thread_id}/runs/{run_id}/submit_tool_outputs
     */
    @POST(OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID_BY_SUBMIT_TOOL_OUTPUTS)
    suspend fun submitToolOuputsToRun(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
