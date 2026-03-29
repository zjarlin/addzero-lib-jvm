package site.addzero.kcloud.api.suno

import de.jensklingenberg.ktorfit.http.*

/**
 * Suno API 全量接口定义（Ktorfit 声明式）
 *
 * Base URL: https://api.sunoapi.org/api/v1/
 * 文档: https://docs.sunoapi.org
 */
interface SunoApi {
    // ── 音乐生成 ─────────────────────────────────────────────
    /** 生成音乐 */
    @Headers("Content-Type: application/json")
    @POST("generate")
    suspend fun generateMusic(
        @Body request: SunoGenerateRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 扩展音乐 */
    @Headers("Content-Type: application/json")
    @POST("generate/extend")
    suspend fun extendMusic(
        @Body request: SunoExtendRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 上传并翻唱 */
    @Headers("Content-Type: application/json")
    @POST("generate/upload-cover")
    suspend fun uploadCover(
        @Body request: SunoUploadCoverRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 上传并扩展 */
    @Headers("Content-Type: application/json")
    @POST("generate/upload-extend")
    suspend fun uploadExtend(
        @Body request: SunoUploadExtendRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 添加人声 */
    @Headers("Content-Type: application/json")
    @POST("generate/add-vocals")
    suspend fun addVocals(
        @Body request: SunoAddVocalsRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 添加乐器 */
    @Headers("Content-Type: application/json")
    @POST("generate/add-instrumental")
    suspend fun addInstrumental(
        @Body request: SunoAddInstrumentalRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 生成音乐封面 */
    @Headers("Content-Type: application/json")
    @POST("generate/music-cover")
    suspend fun generateMusicCover(
        @Body request: SunoMusicCoverRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 替换音乐片段 */
    @Headers("Content-Type: application/json")
    @POST("generate/replace-section")
    suspend fun replaceSection(
        @Body request: SunoReplaceSectionRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    // ── 查询 ─────────────────────────────────────────────────

    /** 查询音乐生成详情 */
    @GET("generate/record-info")
    suspend fun getTaskDetail(
        @Query("taskId") taskId: String,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoTaskDetail>

    /** 查询音乐封面详情 */
    @GET("generate/cover-record-info")
    suspend fun getCoverDetail(
        @Query("taskId") taskId: String,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoTaskDetail>

    // ── 歌词 ─────────────────────────────────────────────────

    /** 生成歌词 */
    @Headers("Content-Type: application/json")
    @POST("lyrics")
    suspend fun generateLyrics(
        @Body request: SunoLyricsRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 查询歌词生成详情 */
    @GET("lyrics/record-info")
    suspend fun getLyricsDetail(
        @Query("taskId") taskId: String,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoLyricsTaskDetail>

    /** 获取带时间戳歌词 */
    @Headers("Content-Type: application/json")
    @POST("get-timestamped-lyrics")
    suspend fun getTimestampedLyrics(
        @Body request: SunoTimestampedLyricsRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoTimestampedLyricsData>

    // ── Persona ──────────────────────────────────────────────

    /** 生成 Persona */
    @Headers("Content-Type: application/json")
    @POST("generate/generate-persona")
    suspend fun generatePersona(
        @Body request: SunoGeneratePersonaRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoPersonaData>

    // ── 音频处理 ─────────────────────────────────────────────

    /** 人声分离 */
    @Headers("Content-Type: application/json")
    @POST("vocal-removal/generate")
    suspend fun vocalRemoval(
        @Body request: SunoVocalRemovalRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    /** 提升音乐风格 */
    @Headers("Content-Type: application/json")
    @POST("boost-music-style")
    suspend fun boostMusicStyle(
        @Body request: SunoBoostStyleRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoBoostStyleData>

    /** WAV 格式转换 */
    @Headers("Content-Type: application/json")
    @POST("wav")
    suspend fun convertToWav(
        @Body request: SunoWavRequest,
        @Header("Authorization") auth: String,
    ): ApiResult<SunoSubmitData>

    // ── 账户 ─────────────────────────────────────────────────

    /** 查询剩余积分 */
    @GET("generate/credit")
    suspend fun getCredits(
        @Header("Authorization") auth: String,
    ): ApiResult<Double>
}
