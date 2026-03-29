package site.addzero.media.playlist.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javafx.application.Platform as JfxPlatform
import javafx.scene.media.Media
import javafx.scene.media.MediaException as JfxMediaException
import javafx.scene.media.MediaPlayer as JfxMediaPlayer
import javafx.util.Duration as JfxDuration
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

@Composable
internal actual fun rememberPlatformPlaylistPlayerEngine(): PlaylistPlayerEngine {
    return remember {
        JvmPlaylistPlayerEngine()
    }
}

private class JvmPlaylistPlayerEngine : PlaylistPlayerEngine {
    private val httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(15))
        .build()

    private var mediaPlayer: JfxMediaPlayer? = null
    private var currentToken = 0L
    private var targetVolume = 1.0
    private var loaderThread: Thread? = null
    private val remoteMediaCache = linkedMapOf<String, File>()

    init {
        JavaFxRuntime.ensureStartedAsync()
    }

    override fun load(
        media: PlaylistEngineMedia,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        stop()
        val token = ++currentToken
        onSnapshot(PlaylistEngineSnapshot(status = PlaylistPlayerStatus.BUFFERING))

        loaderThread = thread(
            start = true,
            name = "playlist-player-media-loader",
            isDaemon = true,
        ) {
            val mediaSource = runCatching {
                resolveMediaSource(media)
            }.getOrElse { error ->
                emitIfCurrent(
                    token = token,
                    onSnapshot = onSnapshot,
                    snapshot = PlaylistEngineSnapshot(
                        status = PlaylistPlayerStatus.ERROR,
                        errorMessage = error.message ?: "音频资源加载失败",
                    ),
                )
                return@thread
            }

            JavaFxRuntime.runLater {
                if (token != currentToken) {
                    return@runLater
                }

                try {
                    val fxMedia = Media(mediaSource)
                    fxMedia.setOnError {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = PlaylistEngineSnapshot(
                                status = PlaylistPlayerStatus.ERROR,
                                errorMessage = fxMedia.error?.message ?: "音频资源加载失败",
                            ),
                        )
                    }

                    val player = JfxMediaPlayer(fxMedia)
                    player.volume = targetVolume
                    mediaPlayer = player

                    player.setOnReady {
                        if (token != currentToken) {
                            player.dispose()
                            return@setOnReady
                        }

                        player.play()
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlaylistPlayerStatus.PLAYING),
                        )
                    }
                    player.setOnPlaying {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlaylistPlayerStatus.PLAYING),
                        )
                    }
                    player.setOnPaused {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlaylistPlayerStatus.PAUSED),
                        )
                    }
                    player.setOnStopped {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlaylistPlayerStatus.IDLE),
                        )
                    }
                    player.setOnStalled {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = player.toSnapshot(PlaylistPlayerStatus.BUFFERING),
                        )
                    }
                    player.setOnEndOfMedia {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = PlaylistEngineSnapshot(
                                status = PlaylistPlayerStatus.ENDED,
                                positionMs = player.totalDuration.safeMillis(),
                                durationMs = player.totalDuration.safeMillis(),
                            ),
                        )
                    }
                    player.setOnError {
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = PlaylistEngineSnapshot(
                                status = PlaylistPlayerStatus.ERROR,
                                positionMs = player.currentTime.safeMillis(),
                                durationMs = player.totalDuration.safeMillis(),
                                errorMessage = player.error?.message ?: "播放器初始化失败",
                            ),
                        )
                    }
                    player.currentTimeProperty().addListener { _, _, newValue ->
                        emitIfCurrent(
                            token = token,
                            onSnapshot = onSnapshot,
                            snapshot = PlaylistEngineSnapshot(
                                status = player.status.toPlaylistPlayerStatus(),
                                positionMs = newValue.safeMillis(),
                                durationMs = player.totalDuration.safeMillis(),
                            ),
                        )
                    }
                } catch (error: JfxMediaException) {
                    emitIfCurrent(
                        token = token,
                        onSnapshot = onSnapshot,
                        snapshot = PlaylistEngineSnapshot(
                            status = PlaylistPlayerStatus.ERROR,
                            errorMessage = error.message,
                        ),
                    )
                }
            }
        }
    }

    override fun play() {
        JavaFxRuntime.runLater {
            mediaPlayer?.play()
        }
    }

    override fun pause() {
        JavaFxRuntime.runLater {
            mediaPlayer?.pause()
        }
    }

    override fun seekTo(positionMs: Long) {
        JavaFxRuntime.runLater {
            mediaPlayer?.seek(JfxDuration.millis(positionMs.toDouble()))
        }
    }

    override fun setVolume(volume: Float) {
        targetVolume = volume.coerceIn(0f, 1f).toDouble()
        JavaFxRuntime.runLater {
            mediaPlayer?.volume = targetVolume
        }
    }

    override fun stop() {
        currentToken++
        loaderThread?.interrupt()
        loaderThread = null
        JavaFxRuntime.runLater {
            mediaPlayer?.stop()
            mediaPlayer?.dispose()
            mediaPlayer = null
        }
    }

    override fun release() {
        stop()
        remoteMediaCache.values.forEach { file ->
            runCatching { file.delete() }
        }
        remoteMediaCache.clear()
    }

    private fun resolveMediaSource(media: PlaylistEngineMedia): String {
        val rawUrl = media.url.trim()
        require(rawUrl.isNotBlank()) { "音频地址不能为空" }

        return when {
            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> {
                resolveRemoteMedia(rawUrl, media.headers)
            }

            rawUrl.startsWith("file:/") -> {
                URI.create(rawUrl).toASCIIString()
            }

            else -> {
                val localFile = File(rawUrl)
                if (localFile.exists()) {
                    localFile.toURI().toString()
                } else {
                    URI.create(rawUrl).toASCIIString()
                }
            }
        }
    }

    private fun resolveRemoteMedia(
        url: String,
        headers: Map<String, String>,
    ): String {
        val cacheKey = buildCacheKey(url, headers)
        remoteMediaCache[cacheKey]
            ?.takeIf(File::exists)
            ?.let { cachedFile ->
                return cachedFile.toURI().toString()
            }

        val requestBuilder = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .timeout(Duration.ofMinutes(2))

        headers.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        val response = httpClient.send(
            requestBuilder.build(),
            HttpResponse.BodyHandlers.ofInputStream(),
        )
        if (response.statusCode() !in 200..299) {
            error("音频资源请求失败: HTTP ${response.statusCode()}")
        }

        val downloadedFile = File.createTempFile("playlist-player-", ".media")
        response.body().use { input ->
            downloadedFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        remoteMediaCache[cacheKey] = downloadedFile
        return downloadedFile.toURI().toString()
    }

    private fun buildCacheKey(
        url: String,
        headers: Map<String, String>,
    ): String {
        return buildString {
            append(url)
            if (headers.isNotEmpty()) {
                append('#')
                headers.toSortedMap().forEach { (key, value) ->
                    append(key)
                    append('=')
                    append(value)
                    append('&')
                }
            }
        }
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

private object JavaFxRuntime {
    private val startRequested = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val pendingActions = ConcurrentLinkedQueue<() -> Unit>()

    fun ensureStartedAsync() {
        if (started.get()) {
            return
        }
        if (!startRequested.compareAndSet(false, true)) {
            return
        }

        thread(
            start = true,
            isDaemon = true,
            name = "playlist-player-javafx-bootstrap",
        ) {
            runCatching {
                JfxPlatform.startup {
                    started.set(true)
                    flushPendingActions()
                }
            }.onFailure { error ->
                if (error.message?.contains("Toolkit already initialized") == true) {
                    started.set(true)
                    flushPendingActions()
                } else {
                    startRequested.set(false)
                    started.set(false)
                }
            }
        }
    }

    fun runLater(action: () -> Unit) {
        if (started.get()) {
            JfxPlatform.runLater(action)
            return
        }

        pendingActions.add(action)
        ensureStartedAsync()
    }

    private fun flushPendingActions() {
        while (true) {
            val pending = pendingActions.poll() ?: break
            JfxPlatform.runLater(pending)
        }
    }
}

private fun JfxMediaPlayer.Status?.toPlaylistPlayerStatus(): PlaylistPlayerStatus {
    return when (this) {
        JfxMediaPlayer.Status.PLAYING -> PlaylistPlayerStatus.PLAYING
        JfxMediaPlayer.Status.PAUSED -> PlaylistPlayerStatus.PAUSED
        JfxMediaPlayer.Status.READY,
        JfxMediaPlayer.Status.STALLED,
        JfxMediaPlayer.Status.UNKNOWN,
        JfxMediaPlayer.Status.HALTED,
        null -> PlaylistPlayerStatus.BUFFERING

        JfxMediaPlayer.Status.STOPPED,
        JfxMediaPlayer.Status.DISPOSED -> PlaylistPlayerStatus.IDLE
    }
}

private fun JfxMediaPlayer.toSnapshot(status: PlaylistPlayerStatus): PlaylistEngineSnapshot {
    return PlaylistEngineSnapshot(
        status = status,
        positionMs = currentTime.safeMillis(),
        durationMs = totalDuration.safeMillis(),
        errorMessage = error?.message,
    )
}

private fun JfxDuration?.safeMillis(): Long {
    val value = this?.toMillis() ?: 0.0
    if (!value.isFinite() || value < 0.0) {
        return 0L
    }
    return value.toLong()
}
