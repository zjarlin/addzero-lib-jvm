package site.addzero.media.playlist.player

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistPlayerControllerTest {
    @Test
    fun resolvedAudioSourceIsCachedAcrossReplays() = runTest {
        val engine = FakePlaylistPlayerEngine()
        val host = PlaylistPlayerHost(engine)
        val controller = PlaylistPlayerController<String>(host)
        var resolveCalls = 0

        controller.bind(
            scope = this,
            items = listOf("a"),
            itemKey = { it },
            titleOf = { "Song $it" },
            subtitleOf = { "Artist" },
            durationMsOf = { 120_000L },
            coverUrlOf = { null },
            hasResolvableAudioOf = { true },
            resolveAudioSource = {
                resolveCalls += 1
                PlaylistAudioSource(url = "https://example.com/$it.mp3")
            },
            resolveLyrics = null,
            resolveErrorMessage = { it.message ?: "加载失败" },
            resolveLyricsErrorMessage = { it.message ?: "歌词失败" },
        )

        controller.play("a")
        advanceUntilIdle()
        assertEquals(1, resolveCalls)
        assertEquals("a", host.snapshot.currentPlaybackId)

        host.stop()
        controller.play("a")
        advanceUntilIdle()

        assertEquals(1, resolveCalls)
        assertEquals(2, engine.loadCount)
    }

    @Test
    fun unavailableAudioIsRememberedAndDisablesFurtherResolve() = runTest {
        val engine = FakePlaylistPlayerEngine()
        val host = PlaylistPlayerHost(engine)
        val controller = PlaylistPlayerController<String>(host)
        var resolveCalls = 0

        controller.bind(
            scope = this,
            items = listOf("silent"),
            itemKey = { it },
            titleOf = { "Silent" },
            subtitleOf = { "Nobody" },
            durationMsOf = { null },
            coverUrlOf = { null },
            hasResolvableAudioOf = { true },
            resolveAudioSource = {
                resolveCalls += 1
                PlaylistAudioSource(
                    url = null,
                    unavailableMessage = "无音源",
                )
            },
            resolveLyrics = null,
            resolveErrorMessage = { it.message ?: "加载失败" },
            resolveLyricsErrorMessage = { it.message ?: "歌词失败" },
        )

        controller.play("silent")
        advanceUntilIdle()
        assertEquals(1, resolveCalls)
        assertTrue(controller.itemState("silent").isUnavailable)

        controller.play("silent")
        advanceUntilIdle()
        assertEquals(1, resolveCalls)
    }

    @Test
    fun playNextMovesToFollowingTrack() = runTest {
        val engine = FakePlaylistPlayerEngine()
        val host = PlaylistPlayerHost(engine)
        val controller = PlaylistPlayerController<String>(host)

        controller.bind(
            scope = this,
            items = listOf("a", "b"),
            itemKey = { it },
            titleOf = { "Song $it" },
            subtitleOf = { "Artist" },
            durationMsOf = { 90_000L },
            coverUrlOf = { null },
            hasResolvableAudioOf = { true },
            resolveAudioSource = { track ->
                PlaylistAudioSource(url = "https://example.com/$track.mp3")
            },
            resolveLyrics = null,
            resolveErrorMessage = { it.message ?: "加载失败" },
            resolveLyricsErrorMessage = { it.message ?: "歌词失败" },
        )

        controller.play("a")
        advanceUntilIdle()
        assertEquals("a", host.snapshot.currentPlaybackId)

        assertTrue(controller.playNext())
        advanceUntilIdle()
        assertEquals("b", host.snapshot.currentPlaybackId)
        assertFalse(controller.playNext())
    }

    @Test
    fun playPreviousMovesBackToEarlierTrack() = runTest {
        val engine = FakePlaylistPlayerEngine()
        val host = PlaylistPlayerHost(engine)
        val controller = PlaylistPlayerController<String>(host)

        controller.bind(
            scope = this,
            items = listOf("a", "b", "c"),
            itemKey = { it },
            titleOf = { "Song $it" },
            subtitleOf = { "Artist" },
            durationMsOf = { 90_000L },
            coverUrlOf = { null },
            hasResolvableAudioOf = { true },
            resolveAudioSource = { track ->
                PlaylistAudioSource(url = "https://example.com/$track.mp3")
            },
            resolveLyrics = null,
            resolveErrorMessage = { it.message ?: "加载失败" },
            resolveLyricsErrorMessage = { it.message ?: "歌词失败" },
        )

        controller.play("b")
        advanceUntilIdle()
        assertEquals("b", host.snapshot.currentPlaybackId)

        assertTrue(controller.playPrevious())
        advanceUntilIdle()
        assertEquals("a", host.snapshot.currentPlaybackId)
        assertFalse(controller.playPrevious())
    }

    @Test
    fun resolveAudioSourceForUsesTheSameSessionCache() = runTest {
        val engine = FakePlaylistPlayerEngine()
        val host = PlaylistPlayerHost(engine)
        val controller = PlaylistPlayerController<String>(host)
        var resolveCalls = 0

        controller.bind(
            scope = this,
            items = listOf("a"),
            itemKey = { it },
            titleOf = { "Song $it" },
            subtitleOf = { "Artist" },
            durationMsOf = { 90_000L },
            coverUrlOf = { null },
            hasResolvableAudioOf = { true },
            resolveAudioSource = {
                resolveCalls += 1
                PlaylistAudioSource(url = "https://example.com/$it.mp3")
            },
            resolveLyrics = null,
            resolveErrorMessage = { it.message ?: "加载失败" },
            resolveLyricsErrorMessage = { it.message ?: "歌词失败" },
        )

        assertEquals("https://example.com/a.mp3", controller.resolveAudioSourceFor("a").url)
        assertEquals("https://example.com/a.mp3", controller.resolveAudioSourceFor("a").url)
        assertEquals(1, resolveCalls)
    }
}

private class FakePlaylistPlayerEngine : PlaylistPlayerEngine {
    var loadCount = 0

    override fun load(
        media: PlaylistEngineMedia,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        loadCount += 1
        onSnapshot(PlaylistEngineSnapshot(status = PlaylistPlayerStatus.BUFFERING))
        onSnapshot(
            PlaylistEngineSnapshot(
                status = PlaylistPlayerStatus.PLAYING,
                durationMs = 120_000L,
            )
        )
    }

    override fun play() = Unit

    override fun pause() = Unit

    override fun seekTo(positionMs: Long) = Unit

    override fun setVolume(volume: Float) = Unit

    override fun stop() = Unit

    override fun release() = Unit
}
