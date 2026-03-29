package site.addzero.kcloud.api.suno

import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════════
//  sunoapi.org  全量 API 数据模型
//  文档: https://docs.sunoapi.org
// ══════════════════════════════════════════════════════════════

// ── 请求模型 ─────────────────────────────────────────────────

/** POST /api/v1/generate — 生成音乐 */
@Serializable
data class SunoGenerateRequest(
    val prompt: String,
    val customMode: Boolean = true,
    val instrumental: Boolean = false,
    val model: String = "V4_5",
    val title: String? = null,
    val style: String? = null,
    val negativeTags: String? = null,
    val vocalGender: String? = "m",
    val personaId: String? = null,
    val callBackUrl: String? = null,
    val styleWeight: Double? = null,
    val weirdnessConstraint: Double? = null,
    val audioWeight: Double? = null,
)

/** POST /api/v1/generate/extend — 扩展音乐 */
@Serializable
data class SunoExtendRequest(
    val audioId: String,
    val model: String = "V4_5",
    val defaultParamFlag: Boolean = true,
    val prompt: String? = null,
    val style: String? = null,
    val title: String? = null,
    val continueAt: Int? = null,
    val negativeTags: String? = null,
    val vocalGender: String? = null,
    val personaId: String? = null,
    val callBackUrl: String? = null,
    val styleWeight: Double? = null,
    val weirdnessConstraint: Double? = null,
    val audioWeight: Double? = null,
)

/** POST /api/v1/generate/upload-cover — 上传并翻唱 */
@Serializable
data class SunoUploadCoverRequest(
    val uploadUrl: String,
    val customMode: Boolean = true,
    val instrumental: Boolean = false,
    val model: String = "V4_5ALL",
    val prompt: String? = null,
    val style: String? = null,
    val title: String? = null,
    val negativeTags: String? = null,
    val vocalGender: String? = null,
    val personaId: String? = null,
    val callBackUrl: String? = null,
    val styleWeight: Double? = null,
    val weirdnessConstraint: Double? = null,
    val audioWeight: Double? = null,
)

/** POST /api/file-url-upload — 从远程 URL 上传文件 */
@Serializable
data class SunoFileUrlUploadRequest(
    val fileUrl: String,
    val uploadPath: String = "audio",
    val fileName: String,
)

/** 文件上传服务返回的文件信息 */
@Serializable
data class SunoUploadedFileData(
    val success: Boolean? = null,
    val fileId: String? = null,
    val fileName: String? = null,
    val originalName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val uploadPath: String? = null,
    val filePath: String? = null,
    val fileUrl: String? = null,
    val downloadUrl: String? = null,
    val uploadTime: String? = null,
    val uploadedAt: String? = null,
    val expiresAt: String? = null,
) {
    val resolvedUrl: String
        get() = fileUrl ?: downloadUrl ?: throw IllegalStateException("文件上传成功，但没有返回可用 URL")
}


/** POST /api/v1/generate/upload-extend — 上传并扩展 */
@Serializable
data class SunoUploadExtendRequest(
    val uploadUrl: String,
    val defaultParamFlag: Boolean = true,
    val instrumental: Boolean = false,
    val model: String = "V4_5ALL",
    val prompt: String? = null,
    val style: String? = null,
    val title: String? = null,
    val continueAt: Int? = null,
    val negativeTags: String? = null,
    val vocalGender: String? = null,
    val personaId: String? = null,
    val callBackUrl: String? = null,
    val styleWeight: Double? = null,
    val weirdnessConstraint: Double? = null,
    val audioWeight: Double? = null,
)

/** POST /api/v1/generate/add-vocals — 添加人声 */
@Serializable
data class SunoAddVocalsRequest(
    val uploadUrl: String,
    val prompt: String? = null,
    val title: String? = null,
    val style: String? = null,
    val negativeTags: String? = null,
    val vocalGender: String? = null,
    val model: String = "V4_5PLUS",
    val callBackUrl: String? = null,
    val styleWeight: Double? = null,
    val weirdnessConstraint: Double? = null,
    val audioWeight: Double? = null,
)

/** POST /api/v1/generate/add-instrumental — 添加乐器 */
@Serializable
data class SunoAddInstrumentalRequest(
    val uploadUrl: String,
    val title: String? = null,
    val tags: String? = null,
    val negativeTags: String? = null,
    val vocalGender: String? = null,
    val model: String = "V4_5PLUS",
    val callBackUrl: String? = null,
    val styleWeight: Double? = null,
    val weirdnessConstraint: Double? = null,
    val audioWeight: Double? = null,
)

/** POST /api/v1/lyrics — 生成歌词 */
@Serializable
data class SunoLyricsRequest(
    val prompt: String,
    val callBackUrl: String? = null,
)

/** POST /api/v1/generate/generate-persona — 生成 Persona */
@Serializable
data class SunoGeneratePersonaRequest(
    val taskId: String,
    val audioId: String,
    val name: String,
    val description: String,
)

/** POST /api/v1/get-timestamped-lyrics — 获取带时间戳歌词 */
@Serializable
data class SunoTimestampedLyricsRequest(
    val taskId: String,
    val audioId: String,
)

/** POST /api/v1/vocal-removal/generate — 人声分离 */
@Serializable
data class SunoVocalRemovalRequest(
    val taskId: String,
    val audioId: String,
    val callBackUrl: String? = null,
)

/** POST /api/v1/boost-music-style — 提升音乐风格 */
@Serializable
data class SunoBoostStyleRequest(
    val taskId: String,
    val audioId: String,
    val callBackUrl: String? = null,
)

/** POST /api/v1/generate/music-cover — 生成音乐封面 */
@Serializable
data class SunoMusicCoverRequest(
    val taskId: String,
    val audioId: String,
    val prompt: String? = null,
    val style: String? = null,
    val title: String? = null,
    val model: String = "V4_5",
    val callBackUrl: String? = null,
)

/** POST /api/v1/generate/replace-section — 替换音乐片段 */
@Serializable
data class SunoReplaceSectionRequest(
    val taskId: String,
    val audioId: String,
    val prompt: String? = null,
    val style: String? = null,
    val replaceStart: Int? = null,
    val replaceEnd: Int? = null,
    val model: String = "V4_5",
    val callBackUrl: String? = null,
)

/** POST /api/v1/wav — WAV 格式转换 */
@Serializable
data class SunoWavRequest(
    val taskId: String,
    val audioId: String,
    val callBackUrl: String? = null,
)

// ── 响应模型 ─────────────────────────────────────────────────

/** 提交任务后的通用响应 data */
@Serializable
data class SunoSubmitData(
    val taskId: String,
)

/** 音乐生成详情中的单首歌曲 */
@Serializable
data class SunoTrack(
    val id: String? = null,
    val audioUrl: String? = null,
    val streamAudioUrl: String? = null,
    val imageUrl: String? = null,
    val prompt: String? = null,
    val modelName: String? = null,
    val title: String? = null,
    val tags: String? = null,
    @Serializable(with = LenientStringSerializer::class)
    val createTime: String? = null,
    val duration: Double? = null,
)

/** 音乐生成详情 response 字段 */
@Serializable
data class SunoTaskResponse(
    val taskId: String? = null,
    val sunoData: List<SunoTrack>? = null,
)

/** GET /api/v1/generate/record-info — 任务详情 */
@Serializable
data class SunoTaskDetail(
    val taskId: String? = null,
    val parentMusicId: String? = null,
    val param: String? = null,
    val response: SunoTaskResponse? = null,
    val status: String? = null,
    val type: String? = null,
    @Serializable(with = LenientStringSerializer::class)
    val errorCode: String? = null,
    val errorMessage: String? = null,
) {
    val isSuccess get() = status == "SUCCESS"
    val isFailed get() = status == "FAILED" ||
        status?.endsWith("_FAILED") == true ||
        !errorMessage.isNullOrBlank() ||
        errorCode != null
    val isProcessing get() = !isSuccess && !isFailed
    val displayStatus: String
        get() = when (status) {
            "SUCCESS" -> "已完成 ✓"
            "FAILED" -> "失败 ✗"
            "QUEUED" -> "排队中..."
            "PROCESSING" -> "生成中..."
            "PENDING" -> "处理中..."
            else -> if (isFailed) "失败 ✗" else status ?: "未知"
        }
    val firstTrack: SunoTrack? get() = response?.sunoData?.firstOrNull()
}

/** 歌词生成详情中的单条歌词 */
@Serializable
data class SunoLyricItem(
    val text: String? = null,
    val title: String? = null,
    val status: String? = null,
    val errorMessage: String? = null,
)

/** 歌词生成详情 response 字段 */
@Serializable
data class SunoLyricsResponse(
    val taskId: String? = null,
    val data: List<SunoLyricItem>? = null,
)

/** 歌词任务详情 */
@Serializable
data class SunoLyricsTaskDetail(
    val taskId: String? = null,
    val param: String? = null,
    val response: SunoLyricsResponse? = null,
    val status: String? = null,
    val type: String? = null,
    @Serializable(with = LenientStringSerializer::class)
    val errorCode: String? = null,
    val errorMessage: String? = null,
) {
    val isSuccess get() = status == "SUCCESS"
    val isFailed get() = status == "FAILED"
}

/** Persona 生成响应 data */
@Serializable
data class SunoPersonaData(
    val personaId: String? = null,
    val name: String? = null,
    val description: String? = null,
)

/** 带时间戳歌词中的单词 */
@Serializable
data class SunoAlignedWord(
    val word: String? = null,
    val success: Boolean = false,
    val startS: Double? = null,
    val endS: Double? = null,
    val palign: Int? = null,
)

/** 带时间戳歌词响应 data */
@Serializable
data class SunoTimestampedLyricsData(
    val alignedWords: List<SunoAlignedWord>? = null,
    val waveformData: List<Double>? = null,
    val hootCer: Double? = null,
    val isStreamed: Boolean = false,
)

/** 积分信息 */
@Serializable
data class SunoCredits(
    val credits: Int = 0,
)

/** 提升风格响应 data */
@Serializable
data class SunoBoostStyleData(
    val taskId: String? = null,
    val param: String? = null,
    val result: String? = null,
    val creditsConsumed: Int? = null,
    val creditsRemaining: Int? = null,
    val successFlag: String? = null,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
    @Serializable(with = LenientStringSerializer::class)
    val createTime: String? = null,
)

// ── UI 辅助 ──────────────────────────────────────────────────

/** 模型版本选项 */
val SUNO_MODELS = listOf("V4_5", "V4_5ALL", "V4_5PLUS", "V4", "V3_5")

/** 性别选项 */
val VOCAL_GENDERS = listOf("m" to "男声", "f" to "女声")
