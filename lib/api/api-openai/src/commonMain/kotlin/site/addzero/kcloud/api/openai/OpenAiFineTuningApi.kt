// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Fine-tuning REST endpoints. */
interface OpenAiFineTuningApi {

    /**
     * Run a grader.
     *
     * REST: POST /fine_tuning/alpha/graders/run
     */
    @POST(OpenAiApiPaths.FINE_TUNING_BY_ALPHA_BY_GRADERS_BY_RUN)
    suspend fun runGrader(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Validate a grader.
     *
     * REST: POST /fine_tuning/alpha/graders/validate
     */
    @POST(OpenAiApiPaths.FINE_TUNING_BY_ALPHA_BY_GRADERS_BY_VALIDATE)
    suspend fun validateGrader(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * **NOTE:** This endpoint requires an [admin API key](../admin-api-keys). Organization owners can use this endpoint to view all permissions for a fine-tuned model checkpoint.
     *
     * REST: GET /fine_tuning/checkpoints/{fine_tuned_model_checkpoint}/permissions
     */
    @GET(OpenAiApiPaths.FINE_TUNING_BY_CHECKPOINTS_BY_FINE_TUNED_MODEL_CHECKPOINT_BY_PERMISSIONS)
    suspend fun listFineTuningCheckpointPermissions(
        @Path("fine_tuned_model_checkpoint") fineTunedModelCheckpoint: String,
        @Query("project_id") projectId: String? = null,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * **NOTE:** Calling this endpoint requires an [admin API key](../admin-api-keys). This enables organization owners to share fine-tuned models with other projects in their organization.
     *
     * REST: POST /fine_tuning/checkpoints/{fine_tuned_model_checkpoint}/permissions
     */
    @POST(OpenAiApiPaths.FINE_TUNING_BY_CHECKPOINTS_BY_FINE_TUNED_MODEL_CHECKPOINT_BY_PERMISSIONS)
    suspend fun createFineTuningCheckpointPermission(
        @Path("fine_tuned_model_checkpoint") fineTunedModelCheckpoint: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * **NOTE:** This endpoint requires an [admin API key](../admin-api-keys). Organization owners can use this endpoint to delete a permission for a fine-tuned model checkpoint.
     *
     * REST: DELETE /fine_tuning/checkpoints/{fine_tuned_model_checkpoint}/permissions/{permission_id}
     */
    @DELETE(OpenAiApiPaths.FINE_TUNING_BY_CHECKPOINTS_BY_FINE_TUNED_MODEL_CHECKPOINT_BY_PERMISSIONS_BY_PERMISSION_ID)
    suspend fun deleteFineTuningCheckpointPermission(
        @Path("fine_tuned_model_checkpoint") fineTunedModelCheckpoint: String,
        @Path("permission_id") permissionId: String
    ): OpenAiResponseBody

    /**
     * List your organization's fine-tuning jobs
     *
     * REST: GET /fine_tuning/jobs
     */
    @GET(OpenAiApiPaths.FINE_TUNING_BY_JOBS)
    suspend fun listPaginatedFineTuningJobs(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("metadata") metadata: OpenAiQueryObject? = null
    ): OpenAiResponseBody

    /**
     * Creates a fine-tuning job which begins the process of creating a new model from a given dataset. Response includes details of the enqueued job including job status and the name of the fine-tuned models once complete. [Learn more about fine-tuning](/docs/guides/model-optimization)
     *
     * REST: POST /fine_tuning/jobs
     */
    @POST(OpenAiApiPaths.FINE_TUNING_BY_JOBS)
    suspend fun createFineTuningJob(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Get info about a fine-tuning job. [Learn more about fine-tuning](/docs/guides/model-optimization)
     *
     * REST: GET /fine_tuning/jobs/{fine_tuning_job_id}
     */
    @GET(OpenAiApiPaths.FINE_TUNING_BY_JOBS_BY_FINE_TUNING_JOB_ID)
    suspend fun retrieveFineTuningJob(
        @Path("fine_tuning_job_id") fineTuningJobId: String
    ): OpenAiResponseBody

    /**
     * Immediately cancel a fine-tune job.
     *
     * REST: POST /fine_tuning/jobs/{fine_tuning_job_id}/cancel
     */
    @POST(OpenAiApiPaths.FINE_TUNING_BY_JOBS_BY_FINE_TUNING_JOB_ID_BY_CANCEL)
    suspend fun cancelFineTuningJob(
        @Path("fine_tuning_job_id") fineTuningJobId: String
    ): OpenAiResponseBody

    /**
     * List checkpoints for a fine-tuning job.
     *
     * REST: GET /fine_tuning/jobs/{fine_tuning_job_id}/checkpoints
     */
    @GET(OpenAiApiPaths.FINE_TUNING_BY_JOBS_BY_FINE_TUNING_JOB_ID_BY_CHECKPOINTS)
    suspend fun listFineTuningJobCheckpoints(
        @Path("fine_tuning_job_id") fineTuningJobId: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null
    ): OpenAiResponseBody

    /**
     * Get status updates for a fine-tuning job.
     *
     * REST: GET /fine_tuning/jobs/{fine_tuning_job_id}/events
     */
    @GET(OpenAiApiPaths.FINE_TUNING_BY_JOBS_BY_FINE_TUNING_JOB_ID_BY_EVENTS)
    suspend fun listFineTuningEvents(
        @Path("fine_tuning_job_id") fineTuningJobId: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null
    ): OpenAiResponseBody

    /**
     * Pause a fine-tune job.
     *
     * REST: POST /fine_tuning/jobs/{fine_tuning_job_id}/pause
     */
    @POST(OpenAiApiPaths.FINE_TUNING_BY_JOBS_BY_FINE_TUNING_JOB_ID_BY_PAUSE)
    suspend fun pauseFineTuningJob(
        @Path("fine_tuning_job_id") fineTuningJobId: String
    ): OpenAiResponseBody

    /**
     * Resume a fine-tune job.
     *
     * REST: POST /fine_tuning/jobs/{fine_tuning_job_id}/resume
     */
    @POST(OpenAiApiPaths.FINE_TUNING_BY_JOBS_BY_FINE_TUNING_JOB_ID_BY_RESUME)
    suspend fun resumeFineTuningJob(
        @Path("fine_tuning_job_id") fineTuningJobId: String
    ): OpenAiResponseBody
}
