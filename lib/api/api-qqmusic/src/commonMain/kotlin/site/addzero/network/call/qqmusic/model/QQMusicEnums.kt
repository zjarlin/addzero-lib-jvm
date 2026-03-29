package site.addzero.network.call.qqmusic.model

/** 搜索类型 */
enum class SearchType(val value: Int) {
    SONG(0),
    ALBUM(2),
    PLAYLIST(3),
    MV(4),
    LYRIC(7),
    USER(8)
}

/** 音质 */
enum class MusicQuality(val prefix: String, val suffix: String) {
    M4A("C400", "m4a"),
    MP3_128("M500", "mp3"),
    MP3_320("M800", "mp3");

    companion object {
        fun fromString(quality: String): MusicQuality = when (quality.lowercase()) {
            "m4a" -> M4A
            "128" -> MP3_128
            else -> MP3_320
        }
    }
}
