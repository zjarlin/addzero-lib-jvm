// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Skills REST endpoints. */
interface OpenAiSkillsApi {

    /**
     * List all skills for the current project.
     *
     * REST: GET /skills
     */
    @GET(OpenAiApiPaths.SKILLS)
    suspend fun listSkills(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null
    ): OpenAiResponseBody

    /**
     * Create a new skill.
     *
     * REST: POST /skills
     */
    @POST(OpenAiApiPaths.SKILLS)
    suspend fun createSkill(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Delete a skill by its ID.
     *
     * REST: DELETE /skills/{skill_id}
     */
    @DELETE(OpenAiApiPaths.SKILLS_BY_SKILL_ID)
    suspend fun deleteSkill(
        @Path("skill_id") skillId: String
    ): OpenAiResponseBody

    /**
     * Get a skill by its ID.
     *
     * REST: GET /skills/{skill_id}
     */
    @GET(OpenAiApiPaths.SKILLS_BY_SKILL_ID)
    suspend fun getSkill(
        @Path("skill_id") skillId: String
    ): OpenAiResponseBody

    /**
     * Update the default version pointer for a skill.
     *
     * REST: POST /skills/{skill_id}
     */
    @POST(OpenAiApiPaths.SKILLS_BY_SKILL_ID)
    suspend fun updateSkillDefaultVersion(
        @Path("skill_id") skillId: String,
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Download a skill zip bundle by its ID.
     *
     * REST: GET /skills/{skill_id}/content
     */
    @GET(OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_CONTENT)
    suspend fun getSkillContent(
        @Path("skill_id") skillId: String
    ): OpenAiBinaryBody

    /**
     * List skill versions for a skill.
     *
     * REST: GET /skills/{skill_id}/versions
     */
    @GET(OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS)
    suspend fun listSkillVersions(
        @Path("skill_id") skillId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null
    ): OpenAiResponseBody

    /**
     * Create a new immutable skill version.
     *
     * REST: POST /skills/{skill_id}/versions
     */
    @POST(OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS)
    suspend fun createSkillVersion(
        @Path("skill_id") skillId: String,
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Delete a skill version.
     *
     * REST: DELETE /skills/{skill_id}/versions/{version}
     */
    @DELETE(OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS_BY_VERSION)
    suspend fun deleteSkillVersion(
        @Path("skill_id") skillId: String,
        @Path("version") version: String
    ): OpenAiResponseBody

    /**
     * Get a specific skill version.
     *
     * REST: GET /skills/{skill_id}/versions/{version}
     */
    @GET(OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS_BY_VERSION)
    suspend fun getSkillVersion(
        @Path("skill_id") skillId: String,
        @Path("version") version: String
    ): OpenAiResponseBody

    /**
     * Download a skill version zip bundle.
     *
     * REST: GET /skills/{skill_id}/versions/{version}/content
     */
    @GET(OpenAiApiPaths.SKILLS_BY_SKILL_ID_BY_VERSIONS_BY_VERSION_BY_CONTENT)
    suspend fun getSkillVersionContent(
        @Path("skill_id") skillId: String,
        @Path("version") version: String
    ): OpenAiBinaryBody
}
