// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Projects REST endpoints. */
interface OpenAiProjectsApi {

    /**
     * Returns a list of projects.
     *
     * REST: GET /organization/projects
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS)
    suspend fun listProjects(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("include_archived") includeArchived: Boolean? = null
    ): OpenAiResponseBody

    /**
     * Create a new project in the organization. Projects can be created and archived, but cannot be deleted.
     *
     * REST: POST /organization/projects
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS)
    suspend fun createProject(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Retrieves a project.
     *
     * REST: GET /organization/projects/{project_id}
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID)
    suspend fun retrieveProject(
        @Path("project_id") projectId: String
    ): OpenAiResponseBody

    /**
     * Modifies a project in the organization.
     *
     * REST: POST /organization/projects/{project_id}
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID)
    suspend fun modifyProject(
        @Path("project_id") projectId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Returns a list of API keys in the project.
     *
     * REST: GET /organization/projects/{project_id}/api_keys
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_API_KEYS)
    suspend fun listProjectApiKeys(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null
    ): OpenAiResponseBody

    /**
     * Deletes an API key from the project. Returns confirmation of the key deletion, or an error if the key belonged to a service account.
     *
     * REST: DELETE /organization/projects/{project_id}/api_keys/{api_key_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_API_KEYS_BY_API_KEY_ID)
    suspend fun deleteProjectApiKey(
        @Path("project_id") projectId: String,
        @Path("api_key_id") apiKeyId: String
    ): OpenAiResponseBody

    /**
     * Retrieves an API key in the project.
     *
     * REST: GET /organization/projects/{project_id}/api_keys/{api_key_id}
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_API_KEYS_BY_API_KEY_ID)
    suspend fun retrieveProjectApiKey(
        @Path("project_id") projectId: String,
        @Path("api_key_id") apiKeyId: String
    ): OpenAiResponseBody

    /**
     * Archives a project in the organization. Archived projects cannot be used or updated.
     *
     * REST: POST /organization/projects/{project_id}/archive
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_ARCHIVE)
    suspend fun archiveProject(
        @Path("project_id") projectId: String
    ): OpenAiResponseBody

    /**
     * Returns the rate limits per model for a project.
     *
     * REST: GET /organization/projects/{project_id}/rate_limits
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_RATE_LIMITS)
    suspend fun listProjectRateLimits(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): OpenAiResponseBody

    /**
     * Updates a project rate limit.
     *
     * REST: POST /organization/projects/{project_id}/rate_limits/{rate_limit_id}
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_RATE_LIMITS_BY_RATE_LIMIT_ID)
    suspend fun updateProjectRateLimits(
        @Path("project_id") projectId: String,
        @Path("rate_limit_id") rateLimitId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Returns a list of service accounts in the project.
     *
     * REST: GET /organization/projects/{project_id}/service_accounts
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_SERVICE_ACCOUNTS)
    suspend fun listProjectServiceAccounts(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null
    ): OpenAiResponseBody

    /**
     * Creates a new service account in the project. This also returns an unredacted API key for the service account.
     *
     * REST: POST /organization/projects/{project_id}/service_accounts
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_SERVICE_ACCOUNTS)
    suspend fun createProjectServiceAccount(
        @Path("project_id") projectId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deletes a service account from the project. Returns confirmation of service account deletion, or an error if the project is archived (archived projects have no service accounts).
     *
     * REST: DELETE /organization/projects/{project_id}/service_accounts/{service_account_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_SERVICE_ACCOUNTS_BY_SERVICE_ACCOUNT_ID)
    suspend fun deleteProjectServiceAccount(
        @Path("project_id") projectId: String,
        @Path("service_account_id") serviceAccountId: String
    ): OpenAiResponseBody

    /**
     * Retrieves a service account in the project.
     *
     * REST: GET /organization/projects/{project_id}/service_accounts/{service_account_id}
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_SERVICE_ACCOUNTS_BY_SERVICE_ACCOUNT_ID)
    suspend fun retrieveProjectServiceAccount(
        @Path("project_id") projectId: String,
        @Path("service_account_id") serviceAccountId: String
    ): OpenAiResponseBody

    /**
     * Returns a list of users in the project.
     *
     * REST: GET /organization/projects/{project_id}/users
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS)
    suspend fun listProjectUsers(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null
    ): OpenAiResponseBody

    /**
     * Adds a user to the project. Users must already be members of the organization to be added to a project.
     *
     * REST: POST /organization/projects/{project_id}/users
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS)
    suspend fun createProjectUser(
        @Path("project_id") projectId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deletes a user from the project. Returns confirmation of project user deletion, or an error if the project is archived (archived projects have no users).
     *
     * REST: DELETE /organization/projects/{project_id}/users/{user_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID)
    suspend fun deleteProjectUser(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String
    ): OpenAiResponseBody

    /**
     * Retrieves a user in the project.
     *
     * REST: GET /organization/projects/{project_id}/users/{user_id}
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID)
    suspend fun retrieveProjectUser(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String
    ): OpenAiResponseBody

    /**
     * Modifies a user's role in the project.
     *
     * REST: POST /organization/projects/{project_id}/users/{user_id}
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID)
    suspend fun modifyProjectUser(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
