package site.addzero.media.playlist.player

import androidx.compose.runtime.Composable

internal data class PlaylistEngineMedia(
    val playbackId: String,
    val url: String,
    val title: String? = null,
    val subtitle: String? = null,
    val coverUrl: String? = null,
    val headers: Map<String, String> = emptyMap(),
)

internal data class PlaylistEngineSnapshot(
    val status: PlaylistPlayerStatus,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val errorMessage: String? = null,
)

internal interface PlaylistPlayerEngine {
    fun load(
        media: PlaylistEngineMedia,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    )

    fun play()

    fun pause()

    fun seekTo(positionMs: Long)

    fun setVolume(volume: Float)

    fun stop()

    fun release()
}

internal class NoOpPlaylistPlayerEngine : PlaylistPlayerEngine {
    override fun load(
        media: PlaylistEngineMedia,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        onSnapshot(
            PlaylistEngineSnapshot(
                status = PlaylistPlayerStatus.ERROR,
                errorMessage = "当前平台暂不支持内置播放",
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

@Composable
internal expect fun rememberPlatformPlaylistPlayerEngine(): PlaylistPlayerEngine
