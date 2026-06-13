// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.UsageResponse

interface OpenAiUsageApi {

    /**
     * Get costs details for the organization. REST: GET /organization/costs
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_COSTS)
    suspend fun usageCosts(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("api_key_ids") apiKeyIds: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): site.addzero.api.openai.models.UsageResponse

    /**
     * Get audio speeches usage details for the organization. REST: GET /organization/usage/audio_speeches
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_AUDIO_SPEECHES)
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
    ): site.addzero.api.openai.models.UsageResponse

    /**
     * Get audio transcriptions usage details for the organization. REST: GET
     * /organization/usage/audio_transcriptions
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_AUDIO_TRANSCRIPTIONS)
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
    ): site.addzero.api.openai.models.UsageResponse

    /**
     * Get code interpreter sessions usage details for the organization. REST: GET
     * /organization/usage/code_interpreter_sessions
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_CODE_INTERPRETER_SESSIONS)
    suspend fun usageCodeInterpreterSessions(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): site.addzero.api.openai.models.UsageResponse

    /**
     * Get completions usage details for the organization. REST: GET /organization/usage/completions
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_COMPLETIONS)
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
    ): site.addzero.api.openai.models.UsageResponse

    /**
     * Get embeddings usage details for the organization. REST: GET /organization/usage/embeddings
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_EMBEDDINGS)
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
    ): site.addzero.api.openai.models.UsageResponse

    /**
     * Get images usage details for the organization. REST: GET /organization/usage/images
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_IMAGES)
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
    ): site.addzero.api.openai.models.UsageResponse

    /**
     * Get moderations usage details for the organization. REST: GET /organization/usage/moderations
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_MODERATIONS)
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
    ): site.addzero.api.openai.models.UsageResponse

    /**
     * Get vector stores usage details for the organization. REST: GET /organization/usage/vector_stores
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USAGE_BY_VECTOR_STORES)
    suspend fun usageVectorStores(
        @Query("start_time") startTime: Int,
        @Query("end_time") endTime: Int? = null,
        @Query("bucket_width") bucketWidth: String? = null,
        @Query("project_ids") projectIds: List<String>? = null,
        @Query("group_by") groupBy: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: String? = null
    ): site.addzero.api.openai.models.UsageResponse

}
