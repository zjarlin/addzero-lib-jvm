// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Usage REST endpoints. */
interface OpenAiUsageApi {

    /**
     * Get costs details for the organization.
     *
     * REST: GET /organization/costs
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_COSTS)
    suspend fun usageCosts(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("api_key_ids") apiKeyIds: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody

    /**
     * Get audio speeches usage details for the organization.
     *
     * REST: GET /organization/usage/audio_speeches
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_AUDIO_SPEECHES)
    suspend fun usageAudioSpeeches(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("user_ids") userIds: List<String>? = null,
        @Query("api_key_ids") apiKeyIds: List<String>? = null,
        @Query("models") models: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody

    /**
     * Get audio transcriptions usage details for the organization.
     *
     * REST: GET /organization/usage/audio_transcriptions
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_AUDIO_TRANSCRIPTIONS)
    suspend fun usageAudioTranscriptions(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("user_ids") userIds: List<String>? = null,
        @Query("api_key_ids") apiKeyIds: List<String>? = null,
        @Query("models") models: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody

    /**
     * Get code interpreter sessions usage details for the organization.
     *
     * REST: GET /organization/usage/code_interpreter_sessions
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_CODE_INTERPRETER_SESSIONS)
    suspend fun usageCodeInterpreterSessions(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody

    /**
     * Get completions usage details for the organization.
     *
     * REST: GET /organization/usage/completions
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_COMPLETIONS)
    suspend fun usageCompletions(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("user_ids") userIds: List<String>? = null,
        @Query("api_key_ids") apiKeyIds: List<String>? = null,
        @Query("models") models: List<String>? = null,
        @Query("batch") batch: Boolean? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody

    /**
     * Get embeddings usage details for the organization.
     *
     * REST: GET /organization/usage/embeddings
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_EMBEDDINGS)
    suspend fun usageEmbeddings(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("user_ids") userIds: List<String>? = null,
        @Query("api_key_ids") apiKeyIds: List<String>? = null,
        @Query("models") models: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody

    /**
     * Get images usage details for the organization.
     *
     * REST: GET /organization/usage/images
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_IMAGES)
    suspend fun usageImages(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("sources") sources: List<String>? = null,
        @Query("sizes") sizes: List<String>? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("user_ids") userIds: List<String>? = null,
        @Query("api_key_ids") apiKeyIds: List<String>? = null,
        @Query("models") models: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody

    /**
     * Get moderations usage details for the organization.
     *
     * REST: GET /organization/usage/moderations
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_MODERATIONS)
    suspend fun usageModerations(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("user_ids") userIds: List<String>? = null,
        @Query("api_key_ids") apiKeyIds: List<String>? = null,
        @Query("models") models: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody

    /**
     * Get vector stores usage details for the organization.
     *
     * REST: GET /organization/usage/vector_stores
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_VECTOR_STORES)
    suspend fun usageVectorStores(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): OpenAiResponseBody
}
