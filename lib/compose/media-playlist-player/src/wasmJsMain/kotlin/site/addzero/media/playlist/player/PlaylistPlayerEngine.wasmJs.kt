package site.addzero.media.playlist.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLAudioElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.math.roundToLong

@Composable
internal actual fun rememberPlatformPlaylistPlayerEngine(): PlaylistPlayerEngine {
    return remember {
        WasmPlaylistPlayerEngine()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private class WasmPlaylistPlayerEngine : PlaylistPlayerEngine {
    private val scope = MainScope()
    private var audioElement: HTMLAudioElement? = null
    private var progressJob: Job? = null
    private var currentToken = 0L
    private var targetVolume = 1f

    override fun load(
        media: PlaylistEngineMedia,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        stop()
        val token = ++currentToken
        onSnapshot(PlaylistEngineSnapshot(status = PlaylistPlayerStatus.BUFFERING))

        val resolvedUrl = runCatching {
            resolvePlayableUrl(media)
        }.getOrElse { error ->
            emitIfCurrent(
                token = token,
                onSnapshot = onSnapshot,
                snapshot = PlaylistEngineSnapshot(
                    status = PlaylistPlayerStatus.ERROR,
                    errorMessage = error.message ?: "音频资源加载失败",
                ),
            )
            return
        }

        val element = (document.createElement("audio") as HTMLAudioElement).apply {
            preload = "auto"
            volume = targetVolume.toDouble()
            src = resolvedUrl
        }
        audioElement = element
        element.load()
        element.play()
        startProgressLoop(token, onSnapshot)
    }

    override fun play() {
        audioElement?.play()
    }

    override fun pause() {
        audioElement?.pause()
    }

    override fun seekTo(positionMs: Long) {
        audioElement?.currentTime = positionMs.coerceAtLeast(0L).toDouble() / 1000.0
    }

    override fun setVolume(volume: Float) {
        targetVolume = volume.coerceIn(0f, 1f)
        audioElement?.volume = targetVolume.toDouble()
    }

    override fun stop() {
        currentToken++
        progressJob?.cancel()
        progressJob = null
        audioElement?.pause()
        audioElement?.src = ""
        audioElement = null
    }

    override fun release() {
        stop()
        scope.cancel()
    }

    private fun resolvePlayableUrl(media: PlaylistEngineMedia): String {
        val rawUrl = media.url.trim()
        require(rawUrl.isNotBlank()) { "音频地址不能为空" }
        require(media.headers.isEmpty()) { "Wasm 暂不支持带鉴权头的私有音源" }
        return rawUrl
    }

    private fun startProgressLoop(
        token: Long,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val element = audioElement ?: break
                val status = when {
                    element.error != null -> PlaylistPlayerStatus.ERROR
                    element.ended -> PlaylistPlayerStatus.ENDED
                    element.paused && element.readyState.toInt() < 2 -> PlaylistPlayerStatus.BUFFERING
                    element.paused -> PlaylistPlayerStatus.PAUSED
                    else -> PlaylistPlayerStatus.PLAYING
                }
                emitIfCurrent(
                    token = token,
                    onSnapshot = onSnapshot,
                    snapshot = PlaylistEngineSnapshot(
                        status = status,
                        positionMs = element.currentPositionMs(),
                        durationMs = element.durationMs(),
                        errorMessage = if (status == PlaylistPlayerStatus.ERROR) {
                            "浏览器音频播放失败"
                        } else {
                            null
                        },
                    ),
                )
                delay(250L)
            }
        }
    }

    private fun HTMLAudioElement.currentPositionMs(): Long {
        val value = currentTime
        if (!value.isFinite() || value < 0.0) {
            return 0L
        }
        return (value * 1000.0).roundToLong()
    }

    private fun HTMLAudioElement.durationMs(): Long {
        val value = duration
        if (!value.isFinite() || value <= 0.0) {
            return 0L
        }
        return (value * 1000.0).roundToLong()
    }

    private fun emitIfCurrent(
        token: Long,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
        snapshot: PlaylistEngineSnapshot,
    ) {
        if (token == currentToken) {
            onSnapshot(snapshot)
        }
    }
}
