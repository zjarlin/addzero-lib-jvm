package site.addzero.media.playlist.player

/**
 * 外部播放器 UI 可消费的播放进度快照。
 */
data class PlaylistPlaybackProgress(
    val playbackKey: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

internal data class ParsedPlaylistLyrics(
    val raw: String,
    val lines: List<PlaylistLyricLine>,
    val hasTimestamps: Boolean,
)

internal data class PlaylistLyricLine(
    val timeMs: Long?,
    val text: String,
)

internal object PlaylistLyricsParser {
    private val timestampRegex = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")

    fun parse(rawLyrics: String?): ParsedPlaylistLyrics? {
        val normalized = rawLyrics?.trim().orEmpty()
        if (normalized.isBlank()) {
            return null
        }

        val parsedLines = buildList {
            normalized.lineSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val matches = timestampRegex.findAll(line).toList()
                    val content = timestampRegex.replace(line, "").trim().ifBlank { "..." }

                    if (matches.isEmpty()) {
                        add(PlaylistLyricLine(timeMs = null, text = content))
                    } else {
                        matches.forEach { match ->
                            add(
                                PlaylistLyricLine(
                                    timeMs = match.toMillis(),
                                    text = content,
                                )
                            )
                        }
                    }
                }
        }

        if (parsedLines.isEmpty()) {
            return null
        }

        val hasTimestamps = parsedLines.any { it.timeMs != null }
        val lines = if (hasTimestamps) {
            parsedLines.sortedWith(compareBy({ it.timeMs ?: Long.MAX_VALUE }, { it.text }))
        } else {
            parsedLines
        }

        return ParsedPlaylistLyrics(
            raw = normalized,
            lines = lines,
            hasTimestamps = hasTimestamps,
        )
    }

    private fun MatchResult.toMillis(): Long {
        val minutes = groupValues[1].toLongOrNull() ?: 0L
        val seconds = groupValues[2].toLongOrNull() ?: 0L
        val fractionText = groupValues.getOrNull(3).orEmpty()
        val millis = when (fractionText.length) {
            0 -> 0L
            1 -> fractionText.toLong() * 100L
            2 -> fractionText.toLong() * 10L
            else -> fractionText.take(3).toLong()
        }
        return minutes * 60_000L + seconds * 1_000L + millis
    }
}

internal fun ParsedPlaylistLyrics.activeIndex(positionMs: Long): Int? {
    if (!hasTimestamps || lines.isEmpty()) {
        return null
    }

    val target = positionMs.coerceAtLeast(0L)
    var candidateIndex: Int? = null

    lines.forEachIndexed { index, line ->
        val lineTime = line.timeMs ?: return@forEachIndexed
        if (lineTime <= target) {
            candidateIndex = index
        }
    }

    return candidateIndex ?: 0
}
