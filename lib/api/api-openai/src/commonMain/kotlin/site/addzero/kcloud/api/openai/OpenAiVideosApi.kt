// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Videos REST endpoints. */
interface OpenAiVideosApi {

    /**
     * List recently generated videos for the current project.
     *
     * REST: GET /videos
     */
    @GET(OpenAiApiPaths.VIDEOS)
    suspend fun listVideos(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null
    ): OpenAiResponseBody

    /**
     * Create a new video generation job from a prompt and optional reference assets.
     *
     * REST: POST /videos
     */
    @POST(OpenAiApiPaths.VIDEOS)
    suspend fun createVideo(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Create a character from an uploaded video.
     *
     * REST: POST /videos/characters
     */
    @POST(OpenAiApiPaths.VIDEOS_BY_CHARACTERS)
    suspend fun createVideoCharacter(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Fetch a character.
     *
     * REST: GET /videos/characters/{character_id}
     */
    @GET(OpenAiApiPaths.VIDEOS_BY_CHARACTERS_BY_CHARACTER_ID)
    suspend fun getVideoCharacter(
        @Path("character_id") characterId: String
    ): OpenAiResponseBody

    /**
     * Create a new video generation job by editing a source video or existing generated video.
     *
     * REST: POST /videos/edits
     */
    @POST(OpenAiApiPaths.VIDEOS_BY_EDITS)
    suspend fun createVideoEdit(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Create an extension of a completed video.
     *
     * REST: POST /videos/extensions
     */
    @POST(OpenAiApiPaths.VIDEOS_BY_EXTENSIONS)
    suspend fun createVideoExtend(
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody

    /**
     * Permanently delete a completed or failed video and its stored assets.
     *
     * REST: DELETE /videos/{video_id}
     */
    @DELETE(OpenAiApiPaths.VIDEOS_BY_VIDEO_ID)
    suspend fun deleteVideo(
        @Path("video_id") videoId: String
    ): OpenAiResponseBody

    /**
     * Fetch the latest metadata for a generated video.
     *
     * REST: GET /videos/{video_id}
     */
    @GET(OpenAiApiPaths.VIDEOS_BY_VIDEO_ID)
    suspend fun getVideo(
        @Path("video_id") videoId: String
    ): OpenAiResponseBody

    /**
     * Download the generated video bytes or a derived preview asset. Streams the rendered video content for the specified video job.
     *
     * REST: GET /videos/{video_id}/content
     */
    @GET(OpenAiApiPaths.VIDEOS_BY_VIDEO_ID_BY_CONTENT)
    suspend fun retrieveVideoContent(
        @Path("video_id") videoId: String,
        @Query("variant") variant: String? = null
    ): OpenAiBinaryBody

    /**
     * Create a remix of a completed video using a refreshed prompt.
     *
     * REST: POST /videos/{video_id}/remix
     */
    @POST(OpenAiApiPaths.VIDEOS_BY_VIDEO_ID_BY_REMIX)
    suspend fun createVideoRemix(
        @Path("video_id") videoId: String,
        @Body body: OpenAiRequestBody? = null
    ): OpenAiResponseBody
}
