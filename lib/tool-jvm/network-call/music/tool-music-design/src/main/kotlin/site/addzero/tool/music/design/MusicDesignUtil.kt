package site.addzero.tool.music.design

import site.addzero.network.call.music.MusicSearchUtil
import site.addzero.network.call.music.model.SongWithLyric
import site.addzero.network.call.suno.Suno
import site.addzero.network.call.suno.model.SunoTask

/**
 * 音乐设计工具类
 * 整合歌词搜索和 Suno 音乐生成功能
 */
object MusicDesignUtil {

    /**
     * 根据歌名搜索歌词并生成新歌曲
     *
     * @param songName 歌名
     * @param artistName 歌手名（可选）
     * @param tags 音乐风格标签
     * @param model Suno 模型版本，默认 chirp-v5
     * @return 生成任务 ID
     */
    fun remixBySongName(
        songName: String,
        artistName: String? = null,
        tags: String = "",
        model: String = "chirp-v5",
    ): String? {
        // 搜索歌词
        val lyricResponse = MusicSearchUtil.getLyric(songName, artistName) ?: return null
        val lyrics = lyricResponse.lrc?.lyric ?: return null

        // 清理歌词（去除时间轴）
        val cleanLyrics = cleanLyricTimestamps(lyrics)

        // 生成音乐
        return Suno.generateMusicCustom(
            prompt = cleanLyrics,
            title = songName,
            tags = tags,
            model = model
        )
    }

    /**
     * 根据歌词片段搜索并生成新歌曲
     *
     * @param lyricFragment 歌词片段
     * @param tags 音乐风格标签
     * @param model Suno 模型版本，默认 chirp-v5
     * @return 生成任务 ID，如果找不到歌词返回 null
     */
    fun remixByLyricFragment(
        lyricFragment: String,
        tags: String = "",
        model: String = "chirp-v5",
    ): String? {
        // 搜索歌词
        val results = MusicSearchUtil.getLyricsByFragment(lyricFragment, limit = 1, filterEmpty = true)
        if (results.isEmpty()) return null

        val result = results.first()
        val lyrics = result.lyric.lrc?.lyric ?: return null

        // 清理歌词（去除时间轴）
        val cleanLyrics = cleanLyricTimestamps(lyrics)

        // 生成音乐
        return Suno.generateMusicCustom(
            prompt = cleanLyrics,
            title = result.song.name,
            tags = tags,
            model = model
        )
    }

    /**
     * 批量根据歌名生成音乐
     *
     * @param songInfos 歌曲信息列表（歌名 to 歌手名）
     * @param tags 音乐风格标签
     * @param model Suno 模型版本，默认 chirp-v5
     * @return 任务 ID 列表
     */
    fun batchRemixBySongNames(
        songInfos: List<Pair<String, String?>>,
        tags: String = "",
        model: String = "chirp-v5",
    ): List<String> {
        return songInfos.mapNotNull { (songName, artistName) ->
            remixBySongName(songName, artistName, tags, model)
        }
    }

    /**
     * 使用现有歌词生成音乐
     *
     * @param lyrics 歌词内容
     * @param title 歌曲标题
     * @param tags 音乐风格标签
     * @param model Suno 模型版本，默认 chirp-v5
     * @return 生成任务 ID
     */
    fun createMusicWithLyrics(
        lyrics: String,
        title: String = "",
        tags: String = "",
        model: String = "chirp-v5",
    ): String {
        // 清理歌词（去除时间轴）
        val cleanLyrics = cleanLyricTimestamps(lyrics)

        return Suno.generateMusicCustom(
            prompt = cleanLyrics,
            title = title,
            tags = tags,
            model = model
        )
    }

    /**
     * 搜索歌曲并获取歌词（用于预览）
     *
     * @param songName 歌名
     * @param artistName 歌手名（可选）
     * @return 歌曲与歌词信息
     */
    fun previewSongLyrics(songName: String, artistName: String? = null): SongWithLyric? {
        val results = MusicSearchUtil.searchWithLyrics(songName, artistName, filterEmpty = true)
        return results.firstOrNull()
    }

    /**
     * 根据歌词片段搜索歌曲（用于预览）
     *
     * @param lyricFragment 歌词片段
     * @param limit 返回数量限制
     * @return 歌曲与歌词列表
     */
    fun previewByLyricFragment(lyricFragment: String, limit: Int = 5): List<SongWithLyric> {
        return MusicSearchUtil.getLyricsByFragment(lyricFragment, limit, filterEmpty = true)
    }

    /**
     * 获取任务信息
     *
     * @param taskId 任务 ID
     * @return 任务信息
     */
    fun fetchTask(taskId: String): SunoTask? {
        return Suno.fetchTask(taskId)
    }

    /**
     * 批量获取任务信息
     *
     * @param taskIds 任务 ID 列表
     * @return 任务信息列表
     */
    fun batchFetchTasks(taskIds: List<String>): List<SunoTask> {
        return Suno.batchFetchTasks(taskIds)
    }

    /**
     * 清理歌词时间轴
     * 将 [00:00.00]歌词内容 格式转换为纯歌词文本
     *
     * @param lyrics 带时间轴的歌词
     * @return 清理后的歌词
     */
    private fun cleanLyricTimestamps(lyrics: String): String {
        return lyrics.lines()
            .map { line ->
                // 移除时间轴标记 [00:00.00]
                line.replace(Regex("\\[\\d{2}:\\d{2}\\.\\d{2,3}]"), "").trim()
            }
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }
}
