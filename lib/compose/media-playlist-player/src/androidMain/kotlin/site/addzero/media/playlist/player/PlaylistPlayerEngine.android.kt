package site.addzero.media.playlist.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberPlatformPlaylistPlayerEngine(): PlaylistPlayerEngine {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        AndroidPlaylistPlayerEngine(context)
    }
}

private class AndroidPlaylistPlayerEngine(
    private val context: Context,
) : PlaylistPlayerEngine {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private var progressTicker: Runnable? = null
    private var currentToken = 0L
    private var targetVolume = 1f

    override fun load(
        media: PlaylistEngineMedia,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        stop()
        val token = ++currentToken
        onSnapshot(PlaylistEngineSnapshot(status = PlaylistPlayerStatus.BUFFERING))

        runCatching {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener {
                    if (token != currentToken) {
                        release()
                        return@setOnPreparedListener
                    }

                    setVolume(targetVolume, targetVolume)
                    start()
                    emitIfCurrent(
                        token = token,
                        onSnapshot = onSnapshot,
                        snapshot = snapshotOf(PlaylistPlayerStatus.PLAYING),
                    )
                    startProgressTicker(token, onSnapshot)
                }
                setOnCompletionListener {
                    emitIfCurrent(
                        token = token,
                        onSnapshot = onSnapshot,
                        snapshot = PlaylistEngineSnapshot(
                            status = PlaylistPlayerStatus.ENDED,
                            positionMs = duration.safeDurationMs(),
                            durationMs = duration.safeDurationMs(),
                        ),
                    )
                }
                setOnErrorListener { _, what, extra ->
                    emitIfCurrent(
                        token = token,
                        onSnapshot = onSnapshot,
                        snapshot = PlaylistEngineSnapshot(
                            status = PlaylistPlayerStatus.ERROR,
                            positionMs = currentPosition.safeDurationMs(),
                            durationMs = duration.safeDurationMs(),
                            errorMessage = "Android MediaPlayer 错误: what=$what extra=$extra",
                        ),
                    )
                    true
                }
                setOnInfoListener { _, what, _ ->
                    when (what) {
                        MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                            emitIfCurrent(
                                token = token,
                                onSnapshot = onSnapshot,
                                snapshot = snapshotOf(PlaylistPlayerStatus.BUFFERING),
                            )
                            true
                        }

                        MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                            emitIfCurrent(
                                token = token,
                                onSnapshot = onSnapshot,
                                snapshot = snapshotOf(
                                    if (isPlaying) PlaylistPlayerStatus.PLAYING else PlaylistPlayerStatus.PAUSED
                                ),
                            )
                            true
                        }

                        else -> false
                    }
                }
            }

            mediaPlayer = player
            val uri = Uri.parse(media.url)
            if (media.headers.isEmpty()) {
                player.setDataSource(context, uri)
            } else {
                player.setDataSource(context, uri, media.headers)
            }
            player.prepareAsync()
        }.onFailure { error ->
            emitIfCurrent(
                token = token,
                onSnapshot = onSnapshot,
                snapshot = PlaylistEngineSnapshot(
                    status = PlaylistPlayerStatus.ERROR,
                    errorMessage = error.message ?: "音频资源加载失败",
                ),
            )
        }
    }

    override fun play() {
        mediaPlayer?.start()
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.coerceAtLeast(0L).toInt())
    }

    override fun setVolume(volume: Float) {
        targetVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(targetVolume, targetVolume)
    }

    override fun stop() {
        currentToken++
        stopProgressTicker()
        mediaPlayer?.runCatching {
            stop()
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun release() {
        stop()
    }

    private fun startProgressTicker(
        token: Long,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        stopProgressTicker()
        val ticker = object : Runnable {
            override fun run() {
                val player = mediaPlayer ?: return
                emitIfCurrent(
                    token = token,
                    onSnapshot = onSnapshot,
                    snapshot = player.snapshotOf(
                        if (player.isPlaying) PlaylistPlayerStatus.PLAYING else PlaylistPlayerStatus.PAUSED
                    ),
                )
                mainHandler.postDelayed(this, 250L)
            }
        }
        progressTicker = ticker
        mainHandler.post(ticker)
    }

    private fun stopProgressTicker() {
        progressTicker?.let(mainHandler::removeCallbacks)
        progressTicker = null
    }

    private fun MediaPlayer.snapshotOf(status: PlaylistPlayerStatus): PlaylistEngineSnapshot {
        return PlaylistEngineSnapshot(
            status = status,
            positionMs = currentPosition.safeDurationMs(),
            durationMs = duration.safeDurationMs(),
        )
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

private fun Int.safeDurationMs(): Long {
    if (this <= 0) {
        return 0L
    }
    return toLong()
}
