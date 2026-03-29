package site.addzero.media.playlist.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.abs

private const val RESTART_THRESHOLD_MS = 350L
private const val SNAPSHOT_POSITION_STEP_MS = 250L

private val LocalPlaylistPlayerHost = staticCompositionLocalOf<PlaylistPlayerHost?> { null }

@Stable
internal class PlaylistPlayerHost(
    private val engine: PlaylistPlayerEngine,
) {
    var snapshot by mutableStateOf(PlaylistPlayerSnapshot())
        private set

    var currentResolvedUrl by mutableStateOf<String?>(null)
        private set

    var currentHeaders by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    private var released = false

    fun play(media: PlaylistEngineMedia) {
        if (released) {
            return
        }

        currentResolvedUrl = media.url
        currentHeaders = media.headers
        snapshot = snapshot.copy(
            currentPlaybackId = media.playbackId,
            title = media.title,
            subtitle = media.subtitle,
            coverUrl = media.coverUrl,
            status = PlaylistPlayerStatus.BUFFERING,
            progress = 0f,
            positionMs = 0L,
            durationMs = 0L,
            errorMessage = null,
        )
        engine.load(media, ::applySnapshot)
        engine.setVolume(snapshot.volume)
    }

    fun pause() {
        if (released) {
            return
        }

        engine.pause()
        snapshot = snapshot.copy(status = PlaylistPlayerStatus.PAUSED)
    }

    fun resume() {
        if (released) {
            return
        }

        if (shouldRestartFromBeginning()) {
            replay()
            return
        }

        engine.play()
        snapshot = snapshot.copy(status = PlaylistPlayerStatus.PLAYING, errorMessage = null)
    }

    fun replay() {
        if (released) {
            return
        }

        engine.seekTo(0L)
        engine.play()
        snapshot = snapshot.copy(
            status = PlaylistPlayerStatus.BUFFERING,
            progress = 0f,
            positionMs = 0L,
            errorMessage = null,
        )
    }

    fun seekTo(positionMs: Long) {
        if (released) {
            return
        }

        val durationMs = snapshot.durationMs
        val target = if (durationMs > 0L) {
            positionMs.coerceIn(0L, durationMs)
        } else {
            positionMs.coerceAtLeast(0L)
        }

        engine.seekTo(target)
        snapshot = snapshot.copy(
            positionMs = target,
            progress = progressOf(target, durationMs),
            errorMessage = null,
        )
    }

    fun setVolume(volume: Float) {
        if (released) {
            return
        }

        val safeVolume = volume.coerceIn(0f, 1f)
        engine.setVolume(safeVolume)
        snapshot = snapshot.copy(volume = safeVolume)
    }

    fun stop() {
        if (released) {
            return
        }

        engine.stop()
        currentResolvedUrl = null
        currentHeaders = emptyMap()
        snapshot = PlaylistPlayerSnapshot(volume = snapshot.volume)
    }

    fun release() {
        if (released) {
            return
        }

        released = true
        engine.release()
        currentResolvedUrl = null
        currentHeaders = emptyMap()
        snapshot = PlaylistPlayerSnapshot(volume = snapshot.volume)
    }

    fun shouldRestartFromBeginning(): Boolean {
        val durationMs = snapshot.durationMs
        if (durationMs <= 0L) {
            return snapshot.status == PlaylistPlayerStatus.ENDED
        }

        return snapshot.status == PlaylistPlayerStatus.ENDED ||
            snapshot.positionMs >= (durationMs - RESTART_THRESHOLD_MS).coerceAtLeast(0L)
    }

    private fun applySnapshot(engineSnapshot: PlaylistEngineSnapshot) {
        if (released) {
            return
        }

        val current = snapshot
        val normalizedPosition = engineSnapshot.positionMs.coerceAtLeast(0L)
        val normalizedDuration = engineSnapshot.durationMs.coerceAtLeast(0L)
        val next = current.copy(
            status = engineSnapshot.status,
            positionMs = normalizedPosition,
            durationMs = normalizedDuration,
            progress = progressOf(normalizedPosition, normalizedDuration),
            errorMessage = engineSnapshot.errorMessage?.trim()?.ifBlank { null },
        )

        val shouldPublish = next.status != current.status ||
            next.durationMs != current.durationMs ||
            next.errorMessage != current.errorMessage ||
            abs(next.positionMs - current.positionMs) >= SNAPSHOT_POSITION_STEP_MS ||
            next.status == PlaylistPlayerStatus.ENDED

        if (shouldPublish) {
            snapshot = next
        }
    }

    private fun progressOf(
        positionMs: Long,
        durationMs: Long,
    ): Float {
        if (durationMs <= 0L) {
            return 0f
        }
        return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }
}

@Composable
fun ProvidePlaylistPlayerHost(
    content: @Composable () -> Unit,
) {
    val host = rememberManagedPlaylistPlayerHost()

    CompositionLocalProvider(
        LocalPlaylistPlayerHost provides host,
        content = content,
    )
}

@Composable
internal fun rememberPlaylistPlayerHost(): PlaylistPlayerHost {
    val providedHost = LocalPlaylistPlayerHost.current
    if (providedHost != null) {
        return providedHost
    }

    return rememberManagedPlaylistPlayerHost()
}

@Composable
private fun rememberManagedPlaylistPlayerHost(): PlaylistPlayerHost {
    val engine = rememberPlatformPlaylistPlayerEngine()
    val managedHost = remember(engine) {
        PlaylistPlayerHost(engine)
    }
    DisposableEffect(managedHost) {
        onDispose {
            managedHost.release()
        }
    }
    return managedHost
}
