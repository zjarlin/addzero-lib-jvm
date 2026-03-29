package site.addzero.media.playlist.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.error
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.status
import platform.AVFoundation.timeControlStatus
import platform.AVFoundation.volume
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL

@Composable
internal actual fun rememberPlatformPlaylistPlayerEngine(): PlaylistPlayerEngine {
    return remember {
        IosPlaylistPlayerEngine()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosPlaylistPlayerEngine : PlaylistPlayerEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: AVPlayer? = null
    private var progressJob: Job? = null
    private var currentToken = 0L
    private var targetVolume = 1f
    private var endObserver: Any? = null

    override fun load(
        media: PlaylistEngineMedia,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        stop()
        val token = ++currentToken
        onSnapshot(PlaylistEngineSnapshot(status = PlaylistPlayerStatus.BUFFERING))

        runCatching {
            val url = requireNotNull(NSURL.URLWithString(media.url.trim())) {
                "音频地址不能为空"
            }
            require(media.headers.isEmpty()) { "iOS 暂不支持带鉴权头的私有音源" }
            val asset = AVURLAsset(
                uRL = url,
                options = null,
            )
            val item = AVPlayerItem(asset = asset)
            val avPlayer = AVPlayer(playerItem = item).apply {
                volume = targetVolume
                play()
            }
            player = avPlayer
            endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = item,
                queue = NSOperationQueue.mainQueue,
            ) { _: NSNotification? ->
                emitIfCurrent(
                    token = token,
                    onSnapshot = onSnapshot,
                    snapshot = avPlayer.snapshotOf(PlaylistPlayerStatus.ENDED),
                )
            }
            startProgressLoop(token, onSnapshot)
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
        player?.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun seekTo(positionMs: Long) {
        player?.seekToTime(
            time = CMTimeMakeWithSeconds(
                seconds = positionMs.coerceAtLeast(0L).toDouble() / 1000.0,
                preferredTimescale = 600,
            ),
        )
    }

    override fun setVolume(volume: Float) {
        targetVolume = volume.coerceIn(0f, 1f)
        player?.volume = targetVolume
    }

    override fun stop() {
        currentToken++
        progressJob?.cancel()
        progressJob = null
        endObserver?.let(NSNotificationCenter.defaultCenter::removeObserver)
        endObserver = null
        player?.pause()
        player?.replaceCurrentItemWithPlayerItem(null)
        player = null
    }

    override fun release() {
        stop()
        scope.cancel()
    }

    private fun startProgressLoop(
        token: Long,
        onSnapshot: (PlaylistEngineSnapshot) -> Unit,
    ) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val avPlayer = player ?: break
                val item = avPlayer.currentItem
                if (item?.status == AVPlayerItemStatusFailed) {
                    emitIfCurrent(
                        token = token,
                        onSnapshot = onSnapshot,
                        snapshot = PlaylistEngineSnapshot(
                            status = PlaylistPlayerStatus.ERROR,
                            positionMs = avPlayer.positionMs(),
                            durationMs = avPlayer.durationMs(),
                            errorMessage = item.error?.localizedDescription ?: "iOS AVPlayer 播放失败",
                        ),
                    )
                    break
                }

                val status = when {
                    avPlayer.isPlaybackFinished() -> PlaylistPlayerStatus.ENDED
                    avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying -> PlaylistPlayerStatus.PLAYING
                    avPlayer.timeControlStatus == AVPlayerTimeControlStatusPaused -> PlaylistPlayerStatus.PAUSED
                    else -> PlaylistPlayerStatus.BUFFERING
                }
                emitIfCurrent(
                    token = token,
                    onSnapshot = onSnapshot,
                    snapshot = avPlayer.snapshotOf(status),
                )
                delay(250L)
            }
        }
    }

    private fun AVPlayer.snapshotOf(status: PlaylistPlayerStatus): PlaylistEngineSnapshot {
        return PlaylistEngineSnapshot(
            status = status,
            positionMs = positionMs(),
            durationMs = durationMs(),
            errorMessage = currentItem?.error?.localizedDescription,
        )
    }

    private fun AVPlayer.positionMs(): Long {
        val seconds = CMTimeGetSeconds(currentTime())
        if (!seconds.isFinite() || seconds < 0.0) {
            return 0L
        }
        return (seconds * 1000.0).toLong()
    }

    private fun AVPlayer.durationMs(): Long {
        val item = currentItem
        val durationTime = if (item != null) {
            item.duration
        } else {
            CMTimeMakeWithSeconds(
                seconds = 0.0,
                preferredTimescale = 1,
            )
        }
        val seconds = CMTimeGetSeconds(durationTime)
        if (!seconds.isFinite() || seconds <= 0.0) {
            return 0L
        }
        return (seconds * 1000.0).toLong()
    }

    private fun AVPlayer.isPlaybackFinished(): Boolean {
        val durationMs = durationMs()
        if (durationMs <= 0L) {
            return false
        }
        return positionMs() >= durationMs - 350L
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
