package site.addzero.network.call.qqmusic

import site.addzero.network.call.qqmusic.model.LyricResponse

/**
 * 解析后的歌词
 */
data class ParsedLyric(
    val ti: String = "",
    val ar: String = "",
    val al: String = "",
    val by: String = "",
    val offset: String = "",
    val count: Int = 0,
    val haveTrans: Boolean = false,
    val lines: List<LyricLine> = emptyList()
)

data class LyricLine(
    val time: String = "",
    val lyric: String = "",
    val trans: String = ""
)

/**
 * 歌词解析器
 * 移植自 qq-music-api.js 的 parseLyric 函数
 */
object LyricParser {

    fun parse(data: LyricResponse): ParsedLyric {
        val haveTrans = data.trans.isNotBlank()
        var lyricLines = data.lyric.split("\n")
        var transLines = data.trans.split("\n")

        var ti = ""
        var ar = ""
        var al = ""
        var by = ""
        var offset = ""

        // 如果第一行不是以 [0 开头，说明有元信息头
        if (lyricLines.isNotEmpty() && !lyricLines[0].startsWith("[0")) {
            ti = extractMeta(lyricLines.getOrNull(0))
            ar = extractMeta(lyricLines.getOrNull(1))
            al = extractMeta(lyricLines.getOrNull(2))
            by = extractMeta(lyricLines.getOrNull(3))
            offset = extractMeta(lyricLines.getOrNull(4))
            lyricLines = lyricLines.drop(5)
            if (haveTrans) transLines = transLines.drop(5)
        }

        val lines = lyricLines.mapIndexed { i, line ->
            LyricLine(
                time = line.substringAfter("[", "").substringBefore("]", ""),
                lyric = line.substringAfter("]", ""),
                trans = if (haveTrans) transLines.getOrNull(i)?.substringAfter("]", "") ?: "" else ""
            )
        }

        return ParsedLyric(
            ti = ti, ar = ar, al = al, by = by, offset = offset,
            count = lines.size, haveTrans = haveTrans, lines = lines
        )
    }

    private fun extractMeta(line: String?): String {
        if (line == null) return ""
        val colonIdx = line.indexOf(':')
        val bracketIdx = line.indexOf(']')
        if (colonIdx < 0 || bracketIdx < 0) return ""
        return line.substring(colonIdx + 1, bracketIdx)
    }
}
