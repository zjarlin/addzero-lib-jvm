// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.AssistantObject
import site.addzero.api.openai.models.CreateAssistantRequest
import site.addzero.api.openai.models.CreateMessageRequest
import site.addzero.api.openai.models.CreateRunRequest
import site.addzero.api.openai.models.CreateThreadAndRunRequest
import site.addzero.api.openai.models.CreateThreadRequest
import site.addzero.api.openai.models.DeleteAssistantResponse
import site.addzero.api.openai.models.DeleteMessageResponse
import site.addzero.api.openai.models.DeleteThreadResponse
import site.addzero.api.openai.models.ListAssistantsResponse
import site.addzero.api.openai.models.ListMessagesResponse
import site.addzero.api.openai.models.ListRunStepsResponse
import site.addzero.api.openai.models.ListRunsResponse
import site.addzero.api.openai.models.MessageObject
import site.addzero.api.openai.models.ModifyAssistantRequest
import site.addzero.api.openai.models.ModifyMessageRequest
import site.addzero.api.openai.models.ModifyRunRequest
import site.addzero.api.openai.models.ModifyThreadRequest
import site.addzero.api.openai.models.RunObject
import site.addzero.api.openai.models.RunStepObject
import site.addzero.api.openai.models.SubmitToolOutputsRunRequest
import site.addzero.api.openai.models.ThreadObject

interface OpenAiAssistantsApi {

    /**
     * Returns a list of assistants. REST: GET /assistants
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ASSISTANTS)
    suspend fun listAssistants(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): site.addzero.api.openai.models.ListAssistantsResponse

    /**
     * Create an assistant with a model and instructions. REST: POST /assistants
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ASSISTANTS)
    suspend fun createAssistant(
        @Body body: site.addzero.api.openai.models.CreateAssistantRequest
    ): site.addzero.api.openai.models.AssistantObject

    /**
     * Retrieves an assistant. REST: GET /assistants/{assistant_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ASSISTANTS_BY_ASSISTANT_ID)
    suspend fun getAssistant(
        @Path("assistant_id") assistantId: String
    ): site.addzero.api.openai.models.AssistantObject

    /**
     * Modifies an assistant. REST: POST /assistants/{assistant_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ASSISTANTS_BY_ASSISTANT_ID)
    suspend fun modifyAssistant(
        @Path("assistant_id") assistantId: String,
        @Body body: site.addzero.api.openai.models.ModifyAssistantRequest
    ): site.addzero.api.openai.models.AssistantObject

    /**
     * Delete an assistant. REST: DELETE /assistants/{assistant_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ASSISTANTS_BY_ASSISTANT_ID)
    suspend fun deleteAssistant(
        @Path("assistant_id") assistantId: String
    ): site.addzero.api.openai.models.DeleteAssistantResponse

    /**
     * Create a thread. REST: POST /threads
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS)
    suspend fun createThread(
        @Body body: site.addzero.api.openai.models.CreateThreadRequest? = null
    ): site.addzero.api.openai.models.ThreadObject

    /**
     * Create a thread and run it in one request. REST: POST /threads/runs
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_RUNS)
    suspend fun createThreadAndRun(
        @Body body: site.addzero.api.openai.models.CreateThreadAndRunRequest
    ): site.addzero.api.openai.models.RunObject

    /**
     * Retrieves a thread. REST: GET /threads/{thread_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID)
    suspend fun getThread(
        @Path("thread_id") threadId: String
    ): site.addzero.api.openai.models.ThreadObject

    /**
     * Modifies a thread. REST: POST /threads/{thread_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID)
    suspend fun modifyThread(
        @Path("thread_id") threadId: String,
        @Body body: site.addzero.api.openai.models.ModifyThreadRequest
    ): site.addzero.api.openai.models.ThreadObject

    /**
     * Delete a thread. REST: DELETE /threads/{thread_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID)
    suspend fun deleteThread(
        @Path("thread_id") threadId: String
    ): site.addzero.api.openai.models.DeleteThreadResponse

    /**
     * Returns a list of messages for a given thread. REST: GET /threads/{thread_id}/messages
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES)
    suspend fun listMessages(
        @Path("thread_id") threadId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("run_id") runId: String? = null
    ): site.addzero.api.openai.models.ListMessagesResponse

    /**
     * Create a message. REST: POST /threads/{thread_id}/messages
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES)
    suspend fun createMessage(
        @Path("thread_id") threadId: String,
        @Body body: site.addzero.api.openai.models.CreateMessageRequest
    ): site.addzero.api.openai.models.MessageObject

    /**
     * Retrieve a message. REST: GET /threads/{thread_id}/messages/{message_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES_BY_MESSAGE_ID)
    suspend fun getMessage(
        @Path("thread_id") threadId: String,
        @Path("message_id") messageId: String
    ): site.addzero.api.openai.models.MessageObject

    /**
     * Modifies a message. REST: POST /threads/{thread_id}/messages/{message_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES_BY_MESSAGE_ID)
    suspend fun modifyMessage(
        @Path("thread_id") threadId: String,
        @Path("message_id") messageId: String,
        @Body body: site.addzero.api.openai.models.ModifyMessageRequest
    ): site.addzero.api.openai.models.MessageObject

    /**
     * Deletes a message. REST: DELETE /threads/{thread_id}/messages/{message_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_MESSAGES_BY_MESSAGE_ID)
    suspend fun deleteMessage(
        @Path("thread_id") threadId: String,
        @Path("message_id") messageId: String
    ): site.addzero.api.openai.models.DeleteMessageResponse

    /**
     * Returns a list of runs belonging to a thread. REST: GET /threads/{thread_id}/runs
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS)
    suspend fun listRuns(
        @Path("thread_id") threadId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): site.addzero.api.openai.models.ListRunsResponse

    /**
     * Create a run. REST: POST /threads/{thread_id}/runs
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS)
    suspend fun createRun(
        @Path("thread_id") threadId: String,
        @Query("include[]") include: List<String>? = null,
        @Body body: site.addzero.api.openai.models.CreateRunRequest
    ): site.addzero.api.openai.models.RunObject

    /**
     * Retrieves a run. REST: GET /threads/{thread_id}/runs/{run_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID)
    suspend fun getRun(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String
    ): site.addzero.api.openai.models.RunObject

    /**
     * Modifies a run. REST: POST /threads/{thread_id}/runs/{run_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID)
    suspend fun modifyRun(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Body body: site.addzero.api.openai.models.ModifyRunRequest
    ): site.addzero.api.openai.models.RunObject

    /**
     * Cancels a run that is `in_progress`. REST: POST /threads/{thread_id}/runs/{run_id}/cancel
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID_BY_CANCEL)
    suspend fun cancelRun(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String
    ): site.addzero.api.openai.models.RunObject

    /**
     * Returns a list of run steps belonging to a run. REST: GET /threads/{thread_id}/runs/{run_id}/steps
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID_BY_STEPS)
    suspend fun listRunSteps(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("include[]") include: List<String>? = null
    ): site.addzero.api.openai.models.ListRunStepsResponse

    /**
     * Retrieves a run step. REST: GET /threads/{thread_id}/runs/{run_id}/steps/{step_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID_BY_STEPS_BY_STEP_ID)
    suspend fun getRunStep(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Path("step_id") stepId: String,
        @Query("include[]") include: List<String>? = null
    ): site.addzero.api.openai.models.RunStepObject

    /**
     * When a run has the `status: "requires_action"` and `required_action.type` is `submit_tool_outputs`,
     * this endpoint can be used to submit the outputs from the tool calls once they're all completed. All
     * outputs must be submitted in a single request. REST: POST
     * /threads/{thread_id}/runs/{run_id}/submit_tool_outputs
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.THREADS_BY_THREAD_ID_BY_RUNS_BY_RUN_ID_BY_SUBMIT_TOOL_OUTPUTS)
    suspend fun submitToolOuputsToRun(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Body body: site.addzero.api.openai.models.SubmitToolOutputsRunRequest
    ): site.addzero.api.openai.models.RunObject

}
