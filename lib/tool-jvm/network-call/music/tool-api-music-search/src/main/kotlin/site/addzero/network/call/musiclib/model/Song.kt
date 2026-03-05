package site.addzero.network.call.musiclib.model

import kotlinx.serialization.Serializable

/**
 * 歌曲数据模型 - 所有音乐源通用的歌曲结构
 */
@Serializable
data class Song(
    val id: String = "",
    val name: String = "",
    val artist: String = "",
    val album: String = "",
    val albumId: String = "",          // 某些源特有，用于获取封面
    val duration: Long = 0,              // 秒
    val size: Long = 0,                 // 文件大小 (字节)
    val bitrate: Long = 0,               // 码率 (kbps)
    val source: String = "",            // kugou, netease, qq, bilibili...
    val url: String = "",               // 真实音频文件下载链接
    val ext: String = "",               // 文件后缀 (mp3, flac...)
    val cover: String = "",             // 封面图片链接
    val link: String = "",              // 歌曲原始链接 (例如网页地址)
    val extra: Map<String, String> = emptyMap(),  // 源特有的元数据
    val isInvalid: Boolean = false      // 标记歌曲是否无效 (经过 Probe 探测后)
) {
    /**
     * 格式化时长 (e.g. 03:45)
     */
    fun formatDuration(): String {
        if (duration == 0.toLong()) return "-"
        val min = duration / 60
        val sec = duration % 60
        return "%02d:%02d".format(min, sec)
    }

    /**
     * 格式化大小 (e.g. 4.5 MB)
     */
    fun formatSize(): String {
        if (size == 0.toLong()) return "-"
        val mb = size.toDouble() / 1024 / 1024
        return "%.2f MB".format(mb)
    }

    /**
     * 格式化码率 (e.g. 320 kbps)
     */
    fun formatBitrate(): String {
        if (bitrate == 0.toLong()) return "-"
        return "$bitrate kbps"
    }

    /**
     * 生成清晰的文件名 (歌手 - 歌名.ext)
     */
    fun filename(): String {
        val extension = ext.ifEmpty { "mp3" }
        return "${sanitizeFilename(name)} - ${sanitizeFilename(artist)}.$extension"
    }

    /**
     * 用于简单的日志打印
     */
    fun display(): String = "$name - $artist"

    companion object {
        /**
         * 清理文件名中的非法字符
         */
        fun sanitizeFilename(name: String): String {
            return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .trim()
        }
    }
}

/**
 * 下载信息
 */
data class DownloadInfo(
    val url: String,
    val playAuth: String = "",
    val format: String = "",
    val size: Long = 0
)

/**
 * 音质枚举
 */
enum class Quality(val bitrate: Int, val description: String) {
    STANDARD(128, "标准音质"),
    HIGH(320, "高音质"),
    LOSSLESS(800, "无损音质"),
    HI_RES(2000, "Hi-Res音质")
}

/**
 * VIP 类型
 */
enum class VipType {
    NONE,       // 非VIP
    NORMAL,     // 普通VIP
    SUPER       // 超级VIP
}
