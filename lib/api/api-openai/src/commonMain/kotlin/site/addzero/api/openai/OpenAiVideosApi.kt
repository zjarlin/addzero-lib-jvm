// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateVideoCharacterBody
import site.addzero.api.openai.models.CreateVideoEditJsonBody
import site.addzero.api.openai.models.CreateVideoExtendJsonBody
import site.addzero.api.openai.models.CreateVideoJsonBody
import site.addzero.api.openai.models.CreateVideoRemixBody
import site.addzero.api.openai.models.DeletedVideoResource
import site.addzero.api.openai.models.OrderEnum
import site.addzero.api.openai.models.VideoCharacterResource
import site.addzero.api.openai.models.VideoContentVariant
import site.addzero.api.openai.models.VideoListResource
import site.addzero.api.openai.models.VideoResource

interface OpenAiVideosApi {

    /**
     * List recently generated videos for the current project. REST: GET /videos
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS)
    suspend fun listVideos(
      @Query("limit") limit: Int? = null,
      @Query("order") order: site.addzero.api.openai.models.OrderEnum? = null,
      @Query("after") after: String? = null
    ): site.addzero.api.openai.models.VideoListResource

    /**
     * Create a new video generation job from a prompt and optional reference assets. REST: POST /videos
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS)
    suspend fun createVideo(
        @Body body: site.addzero.api.openai.models.CreateVideoJsonBody? = null
    ): site.addzero.api.openai.models.VideoResource

    /**
     * Create a character from an uploaded video. REST: POST /videos/characters
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS_BY_CHARACTERS)
    suspend fun createVideoCharacter(
        @Body body: site.addzero.api.openai.models.CreateVideoCharacterBody? = null
    ): site.addzero.api.openai.models.VideoCharacterResource

    /**
     * Fetch a character. REST: GET /videos/characters/{character_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS_BY_CHARACTERS_BY_CHARACTER_ID)
    suspend fun getVideoCharacter(
        @Path("character_id") characterId: String
    ): site.addzero.api.openai.models.VideoCharacterResource

    /**
     * Create a new video generation job by editing a source video or existing generated video. REST: POST
     * /videos/edits
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS_BY_EDITS)
    suspend fun createVideoEdit(
        @Body body: site.addzero.api.openai.models.CreateVideoEditJsonBody? = null
    ): site.addzero.api.openai.models.VideoResource

    /**
     * Create an extension of a completed video. REST: POST /videos/extensions
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS_BY_EXTENSIONS)
    suspend fun createVideoExtend(
        @Body body: site.addzero.api.openai.models.CreateVideoExtendJsonBody? = null
    ): site.addzero.api.openai.models.VideoResource

    /**
     * Fetch the latest metadata for a generated video. REST: GET /videos/{video_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS_BY_VIDEO_ID)
    suspend fun getVideo(
        @Path("video_id") videoId: String
    ): site.addzero.api.openai.models.VideoResource

    /**
     * Permanently delete a completed or failed video and its stored assets. REST: DELETE
     * /videos/{video_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS_BY_VIDEO_ID)
    suspend fun deleteVideo(
        @Path("video_id") videoId: String
    ): site.addzero.api.openai.models.DeletedVideoResource

    /**
     * Download the generated video bytes or a derived preview asset. Streams the rendered video content
     * for the specified video job. REST: GET /videos/{video_id}/content
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS_BY_VIDEO_ID_BY_CONTENT)
    suspend fun retrieveVideoContent(
        @Path("video_id") videoId: String,
        @Query("variant") variant: site.addzero.api.openai.models.VideoContentVariant? = null
    ): String

    /**
     * Create a remix of a completed video using a refreshed prompt. REST: POST /videos/{video_id}/remix
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VIDEOS_BY_VIDEO_ID_BY_REMIX)
    suspend fun createVideoRemix(
        @Path("video_id") videoId: String,
        @Body body: site.addzero.api.openai.models.CreateVideoRemixBody? = null
    ): site.addzero.api.openai.models.VideoResource

}
