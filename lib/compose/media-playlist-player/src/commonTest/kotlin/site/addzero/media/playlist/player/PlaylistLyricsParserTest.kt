package site.addzero.media.playlist.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PlaylistLyricsParserTest {
    @Test
    fun parseTimestampedLyricsAndResolveActiveLine() {
        val lyrics = PlaylistLyricsParser.parse(
            """
            [00:01.00]第一句
            [00:03.50]第二句
            [00:05.00]第三句
            """.trimIndent()
        )

        assertNotNull(lyrics)
        assertEquals(true, lyrics.hasTimestamps)
        assertEquals(0, lyrics.activeIndex(900))
        assertEquals(1, lyrics.activeIndex(3600))
        assertEquals(2, lyrics.activeIndex(5200))
    }
}
