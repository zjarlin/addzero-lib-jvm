// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.Project
import site.addzero.api.openai.models.ProjectApiKey
import site.addzero.api.openai.models.ProjectApiKeyDeleteResponse
import site.addzero.api.openai.models.ProjectApiKeyListResponse
import site.addzero.api.openai.models.ProjectCreateRequest
import site.addzero.api.openai.models.ProjectListResponse
import site.addzero.api.openai.models.ProjectRateLimit
import site.addzero.api.openai.models.ProjectRateLimitListResponse
import site.addzero.api.openai.models.ProjectRateLimitUpdateRequest
import site.addzero.api.openai.models.ProjectServiceAccount
import site.addzero.api.openai.models.ProjectServiceAccountCreateRequest
import site.addzero.api.openai.models.ProjectServiceAccountCreateResponse
import site.addzero.api.openai.models.ProjectServiceAccountDeleteResponse
import site.addzero.api.openai.models.ProjectServiceAccountListResponse
import site.addzero.api.openai.models.ProjectUpdateRequest
import site.addzero.api.openai.models.ProjectUser
import site.addzero.api.openai.models.ProjectUserCreateRequest
import site.addzero.api.openai.models.ProjectUserDeleteResponse
import site.addzero.api.openai.models.ProjectUserListResponse
import site.addzero.api.openai.models.ProjectUserUpdateRequest

interface OpenAiProjectsApi {

    /**
     * Returns a list of projects. REST: GET /organization/projects
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS)
    suspend fun listProjects(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("include_archived") includeArchived: Boolean? = null
    ): site.addzero.api.openai.models.ProjectListResponse

    /**
     * Create a new project in the organization. Projects can be created and archived, but cannot be
     * deleted. REST: POST /organization/projects
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS)
    suspend fun createProject(
        @Body body: site.addzero.api.openai.models.ProjectCreateRequest
    ): site.addzero.api.openai.models.Project

    /**
     * Retrieves a project. REST: GET /organization/projects/{project_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID)
    suspend fun retrieveProject(
        @Path("project_id") projectId: String
    ): site.addzero.api.openai.models.Project

    /**
     * Modifies a project in the organization. REST: POST /organization/projects/{project_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID)
    suspend fun modifyProject(
        @Path("project_id") projectId: String,
        @Body body: site.addzero.api.openai.models.ProjectUpdateRequest
    ): site.addzero.api.openai.models.Project

    /**
     * Returns a list of API keys in the project. REST: GET /organization/projects/{project_id}/api_keys
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_API_KEYS)
    suspend fun listProjectApiKeys(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null
    ): site.addzero.api.openai.models.ProjectApiKeyListResponse

    /**
     * Retrieves an API key in the project. REST: GET
     * /organization/projects/{project_id}/api_keys/{api_key_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_API_KEYS_BY_API_KEY_ID)
    suspend fun retrieveProjectApiKey(
        @Path("project_id") projectId: String,
        @Path("api_key_id") apiKeyId: String
    ): site.addzero.api.openai.models.ProjectApiKey

    /**
     * Deletes an API key from the project. Returns confirmation of the key deletion, or an error if the
     * key belonged to a service account. REST: DELETE
     * /organization/projects/{project_id}/api_keys/{api_key_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_API_KEYS_BY_API_KEY_ID)
    suspend fun deleteProjectApiKey(
        @Path("project_id") projectId: String,
        @Path("api_key_id") apiKeyId: String
    ): site.addzero.api.openai.models.ProjectApiKeyDeleteResponse

    /**
     * Archives a project in the organization. Archived projects cannot be used or updated. REST: POST
     * /organization/projects/{project_id}/archive
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_ARCHIVE)
    suspend fun archiveProject(
        @Path("project_id") projectId: String
    ): site.addzero.api.openai.models.Project

    /**
     * Returns the rate limits per model for a project. REST: GET
     * /organization/projects/{project_id}/rate_limits
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_RATE_LIMITS)
    suspend fun listProjectRateLimits(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): site.addzero.api.openai.models.ProjectRateLimitListResponse

    /**
     * Updates a project rate limit. REST: POST
     * /organization/projects/{project_id}/rate_limits/{rate_limit_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_RATE_LIMITS_BY_RATE_LIMIT_ID)
    suspend fun updateProjectRateLimits(
        @Path("project_id") projectId: String,
        @Path("rate_limit_id") rateLimitId: String,
        @Body body: site.addzero.api.openai.models.ProjectRateLimitUpdateRequest
    ): site.addzero.api.openai.models.ProjectRateLimit

    /**
     * Returns a list of service accounts in the project. REST: GET
     * /organization/projects/{project_id}/service_accounts
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_SERVICE_ACCOUNTS)
    suspend fun listProjectServiceAccounts(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null
    ): site.addzero.api.openai.models.ProjectServiceAccountListResponse

    /**
     * Creates a new service account in the project. This also returns an unredacted API key for the
     * service account. REST: POST /organization/projects/{project_id}/service_accounts
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_SERVICE_ACCOUNTS)
    suspend fun createProjectServiceAccount(
        @Path("project_id") projectId: String,
        @Body body: site.addzero.api.openai.models.ProjectServiceAccountCreateRequest
    ): site.addzero.api.openai.models.ProjectServiceAccountCreateResponse

    /**
     * Retrieves a service account in the project. REST: GET
     * /organization/projects/{project_id}/service_accounts/{service_account_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_SERVICE_ACCOUNTS_BY_SERVICE_ACCOUNT_ID)
    suspend fun retrieveProjectServiceAccount(
        @Path("project_id") projectId: String,
        @Path("service_account_id") serviceAccountId: String
    ): site.addzero.api.openai.models.ProjectServiceAccount

    /**
     * Deletes a service account from the project. Returns confirmation of service account deletion, or an
     * error if the project is archived (archived projects have no service accounts). REST: DELETE
     * /organization/projects/{project_id}/service_accounts/{service_account_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_SERVICE_ACCOUNTS_BY_SERVICE_ACCOUNT_ID)
    suspend fun deleteProjectServiceAccount(
        @Path("project_id") projectId: String,
        @Path("service_account_id") serviceAccountId: String
    ): site.addzero.api.openai.models.ProjectServiceAccountDeleteResponse

    /**
     * Returns a list of users in the project. REST: GET /organization/projects/{project_id}/users
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS)
    suspend fun listProjectUsers(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null
    ): site.addzero.api.openai.models.ProjectUserListResponse

    /**
     * Adds a user to the project. Users must already be members of the organization to be added to a
     * project. REST: POST /organization/projects/{project_id}/users
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS)
    suspend fun createProjectUser(
        @Path("project_id") projectId: String,
        @Body body: site.addzero.api.openai.models.ProjectUserCreateRequest
    ): site.addzero.api.openai.models.ProjectUser

    /**
     * Retrieves a user in the project. REST: GET /organization/projects/{project_id}/users/{user_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID)
    suspend fun retrieveProjectUser(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String
    ): site.addzero.api.openai.models.ProjectUser

    /**
     * Modifies a user's role in the project. REST: POST
     * /organization/projects/{project_id}/users/{user_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID)
    suspend fun modifyProjectUser(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String,
        @Body body: site.addzero.api.openai.models.ProjectUserUpdateRequest
    ): site.addzero.api.openai.models.ProjectUser

    /**
     * Deletes a user from the project. Returns confirmation of project user deletion, or an error if the
     * project is archived (archived projects have no users). REST: DELETE
     * /organization/projects/{project_id}/users/{user_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_USERS_BY_USER_ID)
    suspend fun deleteProjectUser(
        @Path("project_id") projectId: String,
        @Path("user_id") userId: String
    ): site.addzero.api.openai.models.ProjectUserDeleteResponse

}
