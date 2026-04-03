package site.addzero.media.playlist.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

/**
 * 播放器解析后的音源信息。
 */
data class PlaylistAudioSource(
    val url: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val unavailableMessage: String? = null,
)

/**
 * 播放器当前状态。
 */
enum class PlaylistPlayerStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ENDED,
    ERROR,
}

/**
 * 播放器公共快照。
 */
data class PlaylistPlayerSnapshot(
    val currentPlaybackId: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val coverUrl: String? = null,
    val status: PlaylistPlayerStatus = PlaylistPlayerStatus.IDLE,
    val progress: Float = 0f,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 1f,
    val errorMessage: String? = null,
)

/**
 * 旧兼容入口暴露给外部的播放态。
 */
data class PlaylistPlaybackState<T>(
    val selectedItem: T? = null,
    val resolvedUrl: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val isResolving: Boolean = false,
    val resolveError: String? = null,
    val emptyHint: String = "",
)

/**
 * 列表项的动作区状态。
 */
data class PlaylistItemActionState(
    val playbackId: String,
    val canPlay: Boolean,
    val canUseAudioUrl: Boolean,
    val isResolving: Boolean,
    val isUnavailable: Boolean,
    val unavailableMessage: String? = null,
)

/**
 * 组合层记住并绑定播放器控制器的统一入口。
 */
@Composable
fun <T> rememberPlaylistPlayerController(
    items: List<T>,
    itemKey: (T) -> String,
    titleOf: (T) -> String,
    subtitleOf: (T) -> String,
    durationMsOf: (T) -> Long?,
    coverUrlOf: (T) -> String?,
    hasResolvableAudioOf: (T) -> Boolean = { true },
    resolveAudioSource: suspend (T) -> PlaylistAudioSource,
    resolveLyrics: (suspend (T) -> String?)? = null,
    resolveErrorMessage: (Throwable) -> String = { it.message ?: "加载音频失败" },
    resolveLyricsErrorMessage: (Throwable) -> String = { it.message ?: "加载歌词失败" },
): PlaylistPlayerController<T> {
    val host = rememberPlaylistPlayerHost()
    val scope = rememberCoroutineScope()
    val controller = remember(host) {
        PlaylistPlayerController<T>(host)
    }

    controller.bind(
        scope = scope,
        items = items,
        itemKey = itemKey,
        titleOf = titleOf,
        subtitleOf = subtitleOf,
        durationMsOf = durationMsOf,
        coverUrlOf = coverUrlOf,
        hasResolvableAudioOf = hasResolvableAudioOf,
        resolveAudioSource = resolveAudioSource,
        resolveLyrics = resolveLyrics,
        resolveErrorMessage = resolveErrorMessage,
        resolveLyricsErrorMessage = resolveLyricsErrorMessage,
    )

    LaunchedEffect(
        controller.currentPlaybackId,
        controller.snapshot.status,
        resolveLyrics != null,
    ) {
        controller.ensureLyricsLoadedForCurrent()
        controller.autoAdvanceIfNeeded()
    }

    return controller
}

@Stable
internal data class PlaylistItemUiState(
    val isCurrent: Boolean,
    val isResolving: Boolean,
    val isUnavailable: Boolean,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    val isEnded: Boolean,
    val unavailableMessage: String?,
    val statusLabel: String?,
    val buttonLabel: String,
)
