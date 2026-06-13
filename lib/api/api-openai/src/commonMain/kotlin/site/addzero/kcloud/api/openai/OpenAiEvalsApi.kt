// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Evals REST endpoints. */
interface OpenAiEvalsApi {

    /**
     * List evaluations for a project.
     *
     * REST: GET /evals
     */
    @GET(OpenAiApiPaths.EVALS)
    suspend fun listEvals(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("order_by") orderBy: String? = null
    ): OpenAiResponseBody

    /**
     * Create the structure of an evaluation that can be used to test a model's performance. An evaluation is a set of testing criteria and the config for a data source, which dictates the schema of the data used in the evaluation. After creating an evaluation, you can run it on different models and model parameters. We support several types of graders and datasources. For more information, see the [Evals guide](/docs/guides/evals).
     *
     * REST: POST /evals
     */
    @POST(OpenAiApiPaths.EVALS)
    suspend fun createEval(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete an evaluation.
     *
     * REST: DELETE /evals/{eval_id}
     */
    @DELETE(OpenAiApiPaths.EVALS_BY_EVAL_ID)
    suspend fun deleteEval(
        @Path("eval_id") evalId: String
    ): OpenAiResponseBody

    /**
     * Get an evaluation by ID.
     *
     * REST: GET /evals/{eval_id}
     */
    @GET(OpenAiApiPaths.EVALS_BY_EVAL_ID)
    suspend fun getEval(
        @Path("eval_id") evalId: String
    ): OpenAiResponseBody

    /**
     * Update certain properties of an evaluation.
     *
     * REST: POST /evals/{eval_id}
     */
    @POST(OpenAiApiPaths.EVALS_BY_EVAL_ID)
    suspend fun updateEval(
        @Path("eval_id") evalId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Get a list of runs for an evaluation.
     *
     * REST: GET /evals/{eval_id}/runs
     */
    @GET(OpenAiApiPaths.EVALS_BY_EVAL_ID_BY_RUNS)
    suspend fun getEvalRuns(
        @Path("eval_id") evalId: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("status") status: String? = null
    ): OpenAiResponseBody

    /**
     * Kicks off a new run for a given evaluation, specifying the data source, and what model configuration to use to test. The datasource will be validated against the schema specified in the config of the evaluation.
     *
     * REST: POST /evals/{eval_id}/runs
     */
    @POST(OpenAiApiPaths.EVALS_BY_EVAL_ID_BY_RUNS)
    suspend fun createEvalRun(
        @Path("eval_id") evalId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete an eval run.
     *
     * REST: DELETE /evals/{eval_id}/runs/{run_id}
     */
    @DELETE(OpenAiApiPaths.EVALS_BY_EVAL_ID_BY_RUNS_BY_RUN_ID)
    suspend fun deleteEvalRun(
        @Path("eval_id") evalId: String,
        @Path("run_id") runId: String
    ): OpenAiResponseBody

    /**
     * Get an evaluation run by ID.
     *
     * REST: GET /evals/{eval_id}/runs/{run_id}
     */
    @GET(OpenAiApiPaths.EVALS_BY_EVAL_ID_BY_RUNS_BY_RUN_ID)
    suspend fun getEvalRun(
        @Path("eval_id") evalId: String,
        @Path("run_id") runId: String
    ): OpenAiResponseBody

    /**
     * Cancel an ongoing evaluation run.
     *
     * REST: POST /evals/{eval_id}/runs/{run_id}
     */
    @POST(OpenAiApiPaths.EVALS_BY_EVAL_ID_BY_RUNS_BY_RUN_ID)
    suspend fun cancelEvalRun(
        @Path("eval_id") evalId: String,
        @Path("run_id") runId: String
    ): OpenAiResponseBody

    /**
     * Get a list of output items for an evaluation run.
     *
     * REST: GET /evals/{eval_id}/runs/{run_id}/output_items
     */
    @GET(OpenAiApiPaths.EVALS_BY_EVAL_ID_BY_RUNS_BY_RUN_ID_BY_OUTPUT_ITEMS)
    suspend fun getEvalRunOutputItems(
        @Path("eval_id") evalId: String,
        @Path("run_id") runId: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("status") status: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Get an evaluation run output item by ID.
     *
     * REST: GET /evals/{eval_id}/runs/{run_id}/output_items/{output_item_id}
     */
    @GET(OpenAiApiPaths.EVALS_BY_EVAL_ID_BY_RUNS_BY_RUN_ID_BY_OUTPUT_ITEMS_BY_OUTPUT_ITEM_ID)
    suspend fun getEvalRunOutputItem(
        @Path("eval_id") evalId: String,
        @Path("run_id") runId: String,
        @Path("output_item_id") outputItemId: String
    ): OpenAiResponseBody
}
