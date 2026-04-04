package site.addzero.media.playlist.player

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val DEFAULT_UNAVAILABLE_MESSAGE = "当前歌曲无可用音源"

/**
 * 列表播放器的语义化控制器。
 *
 * 对外暴露播放、暂停、进度、切歌和音量控制，对内统一管理音源解析和歌词缓存。
 */
@Stable
class PlaylistPlayerController<T> internal constructor(
    private val host: PlaylistPlayerHost,
) {
    private lateinit var scope: CoroutineScope
    private var items: List<T> = emptyList()
    private var itemKeyOf: (T) -> String = { error("PlaylistPlayerController 尚未绑定 itemKey") }
    private var titleOf: (T) -> String = { "" }
    private var subtitleOf: (T) -> String = { "" }
    private var durationMsOf: (T) -> Long? = { null }
    private var coverUrlOf: (T) -> String? = { null }
    private var hasResolvableAudioOf: (T) -> Boolean = { true }
    private var resolveAudioSource: suspend (T) -> PlaylistAudioSource = { PlaylistAudioSource() }
    private var resolveLyrics: (suspend (T) -> String?)? = null
    private var resolveErrorMessage: (Throwable) -> String = { it.message ?: "加载音频失败" }
    private var resolveLyricsErrorMessage: (Throwable) -> String = { it.message ?: "加载歌词失败" }

    internal val sourceCache = mutableStateMapOf<String, PlaylistAudioSource>()
    internal val unavailableMessages = mutableStateMapOf<String, String>()
    internal val lyricsCache = mutableStateMapOf<String, ParsedPlaylistLyrics>()

    internal var resolvingPlaybackId by mutableStateOf<String?>(null)
    internal var resolveError by mutableStateOf<String?>(null)
    internal var lyricsLoadingPlaybackId by mutableStateOf<String?>(null)
    internal var lyricsErrorPlaybackId by mutableStateOf<String?>(null)
    internal var lyricsError by mutableStateOf<String?>(null)

    private var resolveRequestToken by mutableStateOf(0)
    private var completionHandledPlaybackId by mutableStateOf<String?>(null)

    val snapshot
        get() = host.snapshot

    val currentItem: T?
        get() = currentPlaybackId?.let(::findItemByKey)

    val currentPlaybackId
        get() = snapshot.currentPlaybackId

    internal val currentLyrics
        get() = currentPlaybackId?.let(lyricsCache::get)

    internal val currentResolvedUrl
        get() = host.currentResolvedUrl

    internal val currentHeaders
        get() = host.currentHeaders

    internal fun bind(
        scope: CoroutineScope,
        items: List<T>,
        itemKey: (T) -> String,
        titleOf: (T) -> String,
        subtitleOf: (T) -> String,
        durationMsOf: (T) -> Long?,
        coverUrlOf: (T) -> String?,
        hasResolvableAudioOf: (T) -> Boolean,
        resolveAudioSource: suspend (T) -> PlaylistAudioSource,
        resolveLyrics: (suspend (T) -> String?)?,
        resolveErrorMessage: (Throwable) -> String,
        resolveLyricsErrorMessage: (Throwable) -> String,
    ) {
        this.scope = scope
        this.items = items
        itemKeyOf = itemKey
        this.titleOf = titleOf
        this.subtitleOf = subtitleOf
        this.durationMsOf = durationMsOf
        this.coverUrlOf = coverUrlOf
        this.hasResolvableAudioOf = hasResolvableAudioOf
        this.resolveAudioSource = resolveAudioSource
        this.resolveLyrics = resolveLyrics
        this.resolveErrorMessage = resolveErrorMessage
        this.resolveLyricsErrorMessage = resolveLyricsErrorMessage

        val activeKeys = items.map(itemKey).toSet()
        sourceCache.keys.retainAll(activeKeys)
        unavailableMessages.keys.retainAll(activeKeys)
        lyricsCache.keys.retainAll(activeKeys)
        if (resolvingPlaybackId !in activeKeys) {
            resolvingPlaybackId = null
        }
        if (lyricsLoadingPlaybackId !in activeKeys) {
            lyricsLoadingPlaybackId = null
        }
        if (lyricsErrorPlaybackId !in activeKeys) {
            lyricsErrorPlaybackId = null
            lyricsError = null
        }
    }

    /**
     * 播放指定条目。
     */
    fun play(item: T) {
        val playbackId = itemKeyOf(item)
        resolveError = null

        knownUnavailableMessage(item)?.let {
            resolveError = it
            return
        }

        unavailableMessages[playbackId]?.let {
            resolveError = it
            return
        }

        val currentId = currentPlaybackId
        if (currentId == playbackId) {
            when (snapshot.status) {
                PlaylistPlayerStatus.PLAYING,
                PlaylistPlayerStatus.BUFFERING,
                -> {
                    pause()
                    return
                }

                PlaylistPlayerStatus.PAUSED -> {
                    resume()
                    return
                }

                PlaylistPlayerStatus.ENDED -> {
                    replay()
                    return
                }

                PlaylistPlayerStatus.ERROR,
                PlaylistPlayerStatus.IDLE,
                -> Unit
            }
        }

        val requestToken = ++resolveRequestToken
        resolvingPlaybackId = playbackId
        scope.launch {
            val source = sourceCache[playbackId] ?: runCatching {
                resolveAudioSource(item)
            }.getOrElse { error ->
                if (requestToken == resolveRequestToken) {
                    resolvingPlaybackId = null
                    resolveError = resolveErrorMessage(error)
                }
                return@launch
            }

            sourceCache[playbackId] = source
            if (requestToken != resolveRequestToken) {
                return@launch
            }

            val resolvedUrl = source.url?.trim().orEmpty()
            if (resolvedUrl.isBlank()) {
                val message = source.unavailableMessage?.trim()?.ifBlank { null } ?: DEFAULT_UNAVAILABLE_MESSAGE
                unavailableMessages[playbackId] = message
                resolvingPlaybackId = null
                resolveError = message
                return@launch
            }

            resolvingPlaybackId = null
            unavailableMessages.remove(playbackId)
            resolveError = null
            completionHandledPlaybackId = null
            host.play(
                PlaylistEngineMedia(
                    playbackId = playbackId,
                    url = resolvedUrl,
                    title = titleOf(item),
                    subtitle = subtitleOf(item).ifBlank { null },
                    coverUrl = coverUrlOf(item),
                    headers = source.headers,
                )
            )
        }
    }

    /**
     * 暂停当前播放。
     */
    fun pause() {
        host.pause()
    }

    /**
     * 继续当前播放。
     */
    fun resume() {
        host.resume()
    }

    /**
     * 从头重播当前条目。
     */
    fun replay() {
        host.replay()
    }

    /**
     * 跳转到指定毫秒位置。
     */
    fun seekTo(positionMs: Long) {
        host.seekTo(positionMs)
    }

    /**
     * 按进度百分比跳转。
     */
    fun seekToProgress(progress: Float) {
        val durationMs = snapshot.durationMs
        if (durationMs <= 0L) {
            return
        }
        seekTo((durationMs * progress.coerceIn(0f, 1f)).toLong())
    }

    /**
     * 调整播放器音量。
     */
    fun setVolume(volume: Float) {
        host.setVolume(volume)
    }

    /**
     * 切到下一首。
     *
     * @return 是否成功切换
     */
    fun playNext(): Boolean {
        val playbackId = currentPlaybackId ?: return false
        val currentIndex = items.indexOfFirst { itemKeyOf(it) == playbackId }
        if (currentIndex < 0) {
            return false
        }

        val nextItem = items.getOrNull(currentIndex + 1) ?: return false
        play(nextItem)
        return true
    }

    /**
     * 切到上一首。
     *
     * @return 是否成功切换
     */
    fun playPrevious(): Boolean {
        val playbackId = currentPlaybackId ?: return false
        val currentIndex = items.indexOfFirst { itemKeyOf(it) == playbackId }
        if (currentIndex <= 0) {
            return false
        }

        val previousItem = items.getOrNull(currentIndex - 1) ?: return false
        play(previousItem)
        return true
    }

    internal suspend fun resolveAudioSourceFor(item: T): PlaylistAudioSource {
        val playbackId = itemKeyOf(item)
        knownUnavailableMessage(item)?.let { message ->
            return PlaylistAudioSource(
                url = null,
                unavailableMessage = message,
            )
        }

        sourceCache[playbackId]?.let { cached ->
            return cached
        }

        val source = resolveAudioSource(item)
        sourceCache[playbackId] = source

        val resolvedUrl = source.url?.trim().orEmpty()
        if (resolvedUrl.isBlank()) {
            val message = source.unavailableMessage?.trim()?.ifBlank { null } ?: DEFAULT_UNAVAILABLE_MESSAGE
            unavailableMessages[playbackId] = message
        } else {
            unavailableMessages.remove(playbackId)
        }
        return source
    }

    internal fun itemState(item: T): PlaylistItemUiState {
        val playbackId = itemKeyOf(item)
        val isCurrent = currentPlaybackId == playbackId
        val status = if (isCurrent) {
            snapshot.status
        } else {
            PlaylistPlayerStatus.IDLE
        }
        val unavailableMessage = unavailableMessages[playbackId] ?: knownUnavailableMessage(item)

        return PlaylistItemUiState(
            isCurrent = isCurrent,
            isResolving = resolvingPlaybackId == playbackId,
            isUnavailable = unavailableMessage != null,
            isPlaying = isCurrent && status == PlaylistPlayerStatus.PLAYING,
            isBuffering = isCurrent && status == PlaylistPlayerStatus.BUFFERING,
            isEnded = isCurrent && status == PlaylistPlayerStatus.ENDED,
            unavailableMessage = unavailableMessage,
            statusLabel = when {
                unavailableMessage != null -> unavailableMessage
                isCurrent && status == PlaylistPlayerStatus.BUFFERING -> "正在缓冲"
                isCurrent && status == PlaylistPlayerStatus.PLAYING -> "正在播放"
                isCurrent && status == PlaylistPlayerStatus.PAUSED -> "已暂停"
                isCurrent && status == PlaylistPlayerStatus.ENDED -> "播放结束"
                isCurrent && status == PlaylistPlayerStatus.ERROR -> snapshot.errorMessage ?: "播放失败"
                else -> null
            },
            buttonLabel = when {
                unavailableMessage != null -> "无音源"
                resolvingPlaybackId == playbackId -> "加载中..."
                isCurrent && status == PlaylistPlayerStatus.PLAYING -> "暂停"
                isCurrent && status == PlaylistPlayerStatus.BUFFERING -> "缓冲中..."
                isCurrent && status == PlaylistPlayerStatus.PAUSED -> "继续"
                isCurrent && status == PlaylistPlayerStatus.ENDED -> "重播"
                isCurrent && status == PlaylistPlayerStatus.ERROR -> "重试"
                else -> "试听"
            },
        )
    }

    internal fun itemActionState(item: T): PlaylistItemActionState {
        val playbackId = itemKeyOf(item)
        val itemState = itemState(item)
        return PlaylistItemActionState(
            playbackId = playbackId,
            canPlay = !itemState.isUnavailable && !itemState.isResolving,
            canUseAudioUrl = !itemState.isUnavailable,
            isResolving = itemState.isResolving,
            isUnavailable = itemState.isUnavailable,
            unavailableMessage = itemState.unavailableMessage,
        )
    }

    internal fun currentCompatibilityState(emptyHint: String): PlaylistPlaybackState<T> {
        return PlaylistPlaybackState(
            selectedItem = currentItem,
            resolvedUrl = currentResolvedUrl,
            headers = currentHeaders,
            isResolving = currentPlaybackId != null && resolvingPlaybackId == currentPlaybackId,
            resolveError = resolveError,
            emptyHint = emptyHint,
        )
    }

    internal fun currentProgressOr(default: PlaylistPlaybackProgress): PlaylistPlaybackProgress {
        if (default.playbackKey != null) {
            return default
        }
        return PlaylistPlaybackProgress(
            playbackKey = currentPlaybackId,
            isPlaying = snapshot.status == PlaylistPlayerStatus.PLAYING || snapshot.status == PlaylistPlayerStatus.BUFFERING,
            positionMs = snapshot.positionMs,
            durationMs = snapshot.durationMs,
        )
    }

    internal fun ensureLyricsLoadedForCurrent() {
        val current = currentItem ?: return
        val lyricsResolver = resolveLyrics ?: return
        val playbackId = currentPlaybackId ?: return
        if (lyricsCache.containsKey(playbackId) || lyricsLoadingPlaybackId == playbackId) {
            return
        }

        lyricsLoadingPlaybackId = playbackId
        lyricsErrorPlaybackId = null
        lyricsError = null
        scope.launch {
            runCatching {
                PlaylistLyricsParser.parse(lyricsResolver(current))
            }.onSuccess { parsedLyrics ->
                if (currentPlaybackId == playbackId) {
                    if (parsedLyrics != null) {
                        lyricsCache[playbackId] = parsedLyrics
                    }
                    lyricsLoadingPlaybackId = null
                }
            }.onFailure { error ->
                if (currentPlaybackId == playbackId) {
                    lyricsLoadingPlaybackId = null
                    lyricsErrorPlaybackId = playbackId
                    lyricsError = resolveLyricsErrorMessage(error)
                }
            }
        }
    }

    internal fun autoAdvanceIfNeeded() {
        val playbackId = currentPlaybackId ?: return
        if (snapshot.status != PlaylistPlayerStatus.ENDED) {
            completionHandledPlaybackId = null
            return
        }
        if (completionHandledPlaybackId == playbackId) {
            return
        }

        completionHandledPlaybackId = playbackId
        playNext()
    }

    internal fun durationOf(item: T): Long? = durationMsOf(item)

    internal fun titleOf(item: T): String = titleOf.invoke(item)

    internal fun subtitleOf(item: T): String = subtitleOf.invoke(item)

    internal fun coverOf(item: T): String? = coverUrlOf(item)

    internal fun playbackIdOf(item: T): String = itemKeyOf(item)

    internal fun queueSize(): Int = items.size

    internal fun currentIndexOrNull(): Int? {
        val playbackId = currentPlaybackId ?: return null
        val index = items.indexOfFirst { itemKeyOf(it) == playbackId }
        return index.takeIf { it >= 0 }
    }

    internal fun hasPrevious(): Boolean = (currentIndexOrNull() ?: 0) > 0

    internal fun hasNext(): Boolean {
        val index = currentIndexOrNull() ?: return false
        return index < items.lastIndex
    }

    private fun findItemByKey(playbackId: String): T? {
        return items.firstOrNull { itemKeyOf(it) == playbackId }
    }

    private fun knownUnavailableMessage(item: T): String? {
        if (hasResolvableAudioOf(item)) {
            return null
        }
        return DEFAULT_UNAVAILABLE_MESSAGE
    }
}
