package site.addzero.media.playlist.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun <T> DefaultPlaylistPlayer(
    items: List<T>,
    modifier: Modifier = Modifier,
    controller: PlaylistPlayerController<T>? = null,
    itemKey: (T) -> String,
    titleOf: (T) -> String,
    subtitleOf: (T) -> String,
    durationMsOf: (T) -> Long?,
    coverUrlOf: (T) -> String?,
    hasResolvableAudioOf: (T) -> Boolean = { true },
    resolveAudioSource: suspend (T) -> PlaylistAudioSource,
    emptyHint: String = "从下方列表选择一首歌开始试听",
    resolveErrorMessage: (Throwable) -> String = { it.message ?: "加载音频失败" },
    resolveLyrics: (suspend (T) -> String?)? = null,
    resolveLyricsErrorMessage: (Throwable) -> String = { it.message ?: "加载歌词失败" },
    lyricsEmptyHint: String = "当前歌曲暂无同步歌词",
    itemActions: @Composable RowScope.(T, PlaylistItemActionState) -> Unit = { _, _ -> },
) {
    val activeController = controller ?: rememberPlaylistPlayerController(
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

    PlaylistPlayerLayout(
        items = items,
        modifier = modifier,
        controller = activeController,
        itemKey = itemKey,
        titleOf = titleOf,
        subtitleOf = subtitleOf,
        durationMsOf = durationMsOf,
        coverUrlOf = coverUrlOf,
        emptyHint = emptyHint,
        lyricsEmptyHint = lyricsEmptyHint,
        itemActions = itemActions,
        playbackProgress = activeController.currentProgressOr(PlaylistPlaybackProgress()),
        playerPane = {
            DefaultPlaylistPlayerContent(
                controller = activeController,
                emptyHint = emptyHint,
            )
        },
        showLyrics = resolveLyrics != null,
    )
}

@Deprecated("Prefer DefaultPlaylistPlayer for the built-in high-level player experience.")
@Composable
fun <T> MediaPlaylistPlayer(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemKey: (T) -> String,
    titleOf: (T) -> String,
    artistOf: (T) -> String,
    durationMsOf: (T) -> Long?,
    coverUrlOf: (T) -> String?,
    hasResolvableAudioOf: (T) -> Boolean = { true },
    resolveUrl: suspend (T) -> String,
    headersOf: (T) -> Map<String, String> = { emptyMap() },
    emptyHint: String = "从下方列表选择一首歌开始试听",
    resolveErrorMessage: (Throwable) -> String = { it.message ?: "加载音频失败" },
    resolveLyrics: (suspend (T) -> String?)? = null,
    resolveLyricsErrorMessage: (Throwable) -> String = { it.message ?: "加载歌词失败" },
    lyricsEmptyHint: String = "当前歌曲暂无同步歌词",
    playbackProgress: PlaylistPlaybackProgress = PlaylistPlaybackProgress(),
    playerContent: @Composable (PlaylistPlaybackState<T>) -> Unit = { state ->
        DefaultLegacyPlayerContent(state)
    },
    itemActions: @Composable (T) -> Unit = {},
) {
    val controller = rememberPlaylistPlayerController(
        items = items,
        itemKey = itemKey,
        titleOf = titleOf,
        subtitleOf = artistOf,
        durationMsOf = durationMsOf,
        coverUrlOf = coverUrlOf,
        hasResolvableAudioOf = hasResolvableAudioOf,
        resolveAudioSource = { item ->
            PlaylistAudioSource(
                url = resolveUrl(item),
                headers = headersOf(item),
            )
        },
        resolveLyrics = resolveLyrics,
        resolveErrorMessage = resolveErrorMessage,
        resolveLyricsErrorMessage = resolveLyricsErrorMessage,
    )

    PlaylistPlayerLayout(
        items = items,
        modifier = modifier,
        controller = controller,
        itemKey = itemKey,
        titleOf = titleOf,
        subtitleOf = artistOf,
        durationMsOf = durationMsOf,
        coverUrlOf = coverUrlOf,
        emptyHint = emptyHint,
        lyricsEmptyHint = lyricsEmptyHint,
        itemActions = { item, _ ->
            itemActions(item)
        },
        playbackProgress = controller.currentProgressOr(playbackProgress),
        playerPane = {
            playerContent(controller.currentCompatibilityState(emptyHint))
        },
        showLyrics = resolveLyrics != null,
    )
}

@Composable
private fun <T> PlaylistPlayerLayout(
    items: List<T>,
    modifier: Modifier,
    controller: PlaylistPlayerController<T>,
    itemKey: (T) -> String,
    titleOf: (T) -> String,
    subtitleOf: (T) -> String,
    durationMsOf: (T) -> Long?,
    coverUrlOf: (T) -> String?,
    emptyHint: String,
    lyricsEmptyHint: String,
    itemActions: @Composable RowScope.(T, PlaylistItemActionState) -> Unit,
    playbackProgress: PlaylistPlaybackProgress,
    showLyrics: Boolean,
    playerPane: @Composable () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val currentItem = controller.currentItem
    val snapshot = controller.snapshot
    val displayTitle = currentItem?.let(titleOf) ?: snapshot.title ?: "列表播放器"
    val displaySubtitle = currentItem?.let(subtitleOf)?.ifBlank { null } ?: snapshot.subtitle
    val displayCover = currentItem?.let(coverUrlOf) ?: snapshot.coverUrl
    val displayDuration = currentItem?.let(durationMsOf)
        ?: snapshot.durationMs.takeIf { it > 0L }
    var playlistActionMessage by remember { mutableStateOf<String?>(null) }
    var playlistUtilityBusy by remember { mutableStateOf(false) }
    var urlDialogState by remember { mutableStateOf<PlaylistUrlDialogState?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BoxWithConstraints {
            val wideLayout = maxWidth >= 1080.dp && showLyrics
            if (wideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            PlaylistPlayerHeader(
                                title = displayTitle,
                                subtitle = displaySubtitle,
                                coverUrl = displayCover,
                                durationMs = displayDuration,
                            )
                            playerPane()
                        }
                    }
                    ElevatedCard(
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                        ) {
                            PlaylistLyricsCard(
                                lyrics = controller.currentLyrics,
                                loading = controller.lyricsLoadingPlaybackId == controller.currentPlaybackId,
                                error = controller.lyricsError.takeIf {
                                    controller.lyricsErrorPlaybackId == controller.currentPlaybackId
                                },
                                playbackProgress = playbackProgress,
                                emptyHint = lyricsEmptyHint,
                            )
                        }
                    }
                }
            } else {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PlaylistPlayerHeader(
                            title = displayTitle,
                            subtitle = displaySubtitle,
                            coverUrl = displayCover,
                            durationMs = displayDuration,
                        )
                        playerPane()
                        if (showLyrics) {
                            PlaylistLyricsCard(
                                lyrics = controller.currentLyrics,
                                loading = controller.lyricsLoadingPlaybackId == controller.currentPlaybackId,
                                error = controller.lyricsError.takeIf {
                                    controller.lyricsErrorPlaybackId == controller.currentPlaybackId
                                },
                                playbackProgress = playbackProgress,
                                emptyHint = lyricsEmptyHint,
                            )
                        }
                    }
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "播放列表",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (items.isEmpty()) {
                                "还没有可播放的内容"
                            } else {
                                "共 ${items.size} 首，支持列表联动播放"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    controller.currentIndexOrNull()?.let { currentIndex ->
                        PlaylistMetadataPill(text = "${currentIndex + 1}/${items.size}")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalButton(
                        onClick = {
                            if (playlistUtilityBusy) {
                                return@FilledTonalButton
                            }
                            playlistUtilityBusy = true
                            playlistActionMessage = null
                            scope.launch {
                                val resolvedUrls = buildList {
                                    items.forEach { item ->
                                        runCatching {
                                            controller.resolveAudioSourceFor(item)
                                        }.getOrNull()
                                            ?.url
                                            ?.trim()
                                            ?.takeIf(String::isNotBlank)
                                            ?.let(::add)
                                    }
                                }
                                playlistUtilityBusy = false
                                if (resolvedUrls.isEmpty()) {
                                    playlistActionMessage = "当前列表里没有可复制的歌曲 URL"
                                    return@launch
                                }
                                clipboardManager.setText(
                                    AnnotatedString(resolvedUrls.joinToString(separator = "\n"))
                                )
                                playlistActionMessage = "已复制 ${resolvedUrls.size} 条歌曲 URL"
                            }
                        },
                        enabled = items.isNotEmpty() && !playlistUtilityBusy,
                    ) {
                        Text(if (playlistUtilityBusy) "处理中..." else "复制全部 URL")
                    }
                }

                playlistActionMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (items.isEmpty()) {
                    Text(
                        text = emptyHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = items,
                            key = { item -> itemKey(item) },
                        ) { item ->
                            val itemState = controller.itemState(item)
                            val itemActionState = controller.itemActionState(item)
                            PlaylistListItem(
                                title = titleOf(item),
                                subtitle = subtitleOf(item),
                                durationMs = durationMsOf(item),
                                coverUrl = coverUrlOf(item),
                                state = itemState,
                                onPlayClick = { controller.play(item) },
                                itemActions = {
                                    val displayTitleText = titleOf(item)
                                    val displaySubtitleText = subtitleOf(item)

                                    FilledTonalButton(
                                        onClick = {
                                            if (playlistUtilityBusy) {
                                                return@FilledTonalButton
                                            }
                                            playlistUtilityBusy = true
                                            playlistActionMessage = null
                                            scope.launch {
                                                val source = runCatching {
                                                    controller.resolveAudioSourceFor(item)
                                                }.getOrElse { error ->
                                                    playlistActionMessage = error.message ?: "读取歌曲 URL 失败"
                                                    playlistUtilityBusy = false
                                                    return@launch
                                                }
                                                playlistUtilityBusy = false
                                                val resolvedUrl = source.url?.trim().orEmpty()
                                                if (resolvedUrl.isBlank()) {
                                                    playlistActionMessage =
                                                        source.unavailableMessage ?: "当前歌曲没有可用音源 URL"
                                                    return@launch
                                                }
                                                urlDialogState = PlaylistUrlDialogState(
                                                    title = displayTitleText,
                                                    subtitle = displaySubtitleText,
                                                    playbackId = controller.playbackIdOf(item),
                                                    url = resolvedUrl,
                                                )
                                            }
                                        },
                                        enabled = itemActionState.canUseAudioUrl && !playlistUtilityBusy,
                                    ) {
                                        Text("链接")
                                    }

                                    TextButton(
                                        onClick = {
                                            if (playlistUtilityBusy) {
                                                return@TextButton
                                            }
                                            playlistUtilityBusy = true
                                            playlistActionMessage = null
                                            scope.launch {
                                                val source = runCatching {
                                                    controller.resolveAudioSourceFor(item)
                                                }.getOrElse { error ->
                                                    playlistActionMessage = error.message ?: "复制歌曲 URL 失败"
                                                    playlistUtilityBusy = false
                                                    return@launch
                                                }
                                                playlistUtilityBusy = false
                                                val resolvedUrl = source.url?.trim().orEmpty()
                                                if (resolvedUrl.isBlank()) {
                                                    playlistActionMessage =
                                                        source.unavailableMessage ?: "当前歌曲没有可用音源 URL"
                                                    return@launch
                                                }
                                                clipboardManager.setText(AnnotatedString(resolvedUrl))
                                                playlistActionMessage = "已复制 $displayTitleText 的歌曲 URL"
                                            }
                                        },
                                        enabled = itemActionState.canUseAudioUrl && !playlistUtilityBusy,
                                    ) {
                                        Text("复制")
                                    }

                                    itemActions(item, itemActionState)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    urlDialogState?.let { dialogState ->
        PlaylistUrlDialog(
            state = dialogState,
            onDismiss = { urlDialogState = null },
            onCopy = {
                clipboardManager.setText(AnnotatedString(dialogState.url))
                playlistActionMessage = "已复制 ${dialogState.title} 的歌曲 URL"
                urlDialogState = null
            },
        )
    }
}

@Composable
private fun <T> DefaultPlaylistPlayerContent(
    controller: PlaylistPlayerController<T>,
    emptyHint: String,
) {
    val snapshot = controller.snapshot
    val hasPlayback = snapshot.currentPlaybackId != null && controller.currentResolvedUrl != null
    val progressEnabled = snapshot.durationMs > 0L
    val currentIndex = controller.currentIndexOrNull()
    val queueLabel = if (currentIndex != null && controller.queueSize() > 0) {
        "队列 ${currentIndex + 1}/${controller.queueSize()}"
    } else {
        "等待选择歌曲"
    }
    var draggingProgress by remember(snapshot.currentPlaybackId) { mutableStateOf<Float?>(null) }
    val sliderProgress = (draggingProgress ?: snapshot.progress).coerceIn(0f, 1f)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            controller.resolvingPlaybackId != null && snapshot.currentPlaybackId == null -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Text(
                        text = "正在解析音频...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            hasPlayback -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "Now Playing",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = playbackStatusDescription(
                                        status = snapshot.status,
                                        errorMessage = snapshot.errorMessage,
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = queueLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            PlaylistStatusBadge(snapshot.status)
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = formatPlaylistDuration(snapshot.positionMs),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = if (snapshot.durationMs > 0L) {
                                        formatPlaylistDuration(snapshot.durationMs)
                                    } else {
                                        "--:--"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Slider(
                                value = sliderProgress,
                                onValueChange = { newValue ->
                                    if (progressEnabled) {
                                        draggingProgress = newValue
                                    }
                                },
                                onValueChangeFinished = {
                                    val pendingValue = draggingProgress
                                    draggingProgress = null
                                    if (progressEnabled && pendingValue != null) {
                                        controller.seekToProgress(pendingValue)
                                    }
                                },
                                enabled = progressEnabled,
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                ),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = compactPlaybackStatusText(
                                        status = snapshot.status,
                                        errorMessage = snapshot.errorMessage,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (snapshot.status == PlaylistPlayerStatus.ERROR) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                                currentIndex?.let {
                                    PlaylistMetadataPill(text = "${it + 1}/${controller.queueSize()}")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PlaylistTransportButton(
                                    icon = Icons.Filled.SkipPrevious,
                                    contentDescription = "上一首",
                                    enabled = controller.hasPrevious(),
                                    onClick = controller::playPrevious,
                                )
                                FilledIconButton(
                                    onClick = {
                                        when (snapshot.status) {
                                            PlaylistPlayerStatus.PLAYING,
                                            PlaylistPlayerStatus.BUFFERING,
                                            -> controller.pause()

                                            PlaylistPlayerStatus.ENDED -> controller.replay()
                                            PlaylistPlayerStatus.PAUSED -> controller.resume()
                                            PlaylistPlayerStatus.ERROR -> {
                                                val currentItem = controller.currentItem
                                                if (currentItem != null) {
                                                    controller.play(currentItem)
                                                } else {
                                                    controller.replay()
                                                }
                                            }

                                            PlaylistPlayerStatus.IDLE -> controller.resume()
                                        }
                                    },
                                    modifier = Modifier.size(64.dp),
                                ) {
                                    Icon(
                                        imageVector = when (snapshot.status) {
                                            PlaylistPlayerStatus.PLAYING,
                                            PlaylistPlayerStatus.BUFFERING,
                                            -> Icons.Filled.Pause

                                            PlaylistPlayerStatus.ENDED -> Icons.Filled.Replay
                                            else -> Icons.Filled.PlayArrow
                                        },
                                        contentDescription = "播放控制",
                                        modifier = Modifier.size(34.dp),
                                    )
                                }
                                PlaylistTransportButton(
                                    icon = Icons.Filled.SkipNext,
                                    contentDescription = "下一首",
                                    enabled = controller.hasNext(),
                                    onClick = controller::playNext,
                                )
                            }
                        }

                        PlaylistVolumeRow(
                            volume = snapshot.volume,
                            onVolumeChange = controller::setVolume,
                        )
                    }
                }
            }

            else -> {
                Text(
                    text = emptyHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        controller.resolveError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun PlaylistStatusBadge(status: PlaylistPlayerStatus) {
    val label = when (status) {
        PlaylistPlayerStatus.PLAYING -> "播放中"
        PlaylistPlayerStatus.BUFFERING -> "缓冲中"
        PlaylistPlayerStatus.PAUSED -> "已暂停"
        PlaylistPlayerStatus.ENDED -> "已结束"
        PlaylistPlayerStatus.ERROR -> "播放异常"
        PlaylistPlayerStatus.IDLE -> "未播放"
    }
    val containerColor = when (status) {
        PlaylistPlayerStatus.PLAYING -> MaterialTheme.colorScheme.primaryContainer
        PlaylistPlayerStatus.BUFFERING -> MaterialTheme.colorScheme.secondaryContainer
        PlaylistPlayerStatus.PAUSED -> MaterialTheme.colorScheme.surfaceVariant
        PlaylistPlayerStatus.ENDED -> MaterialTheme.colorScheme.tertiaryContainer
        PlaylistPlayerStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
        PlaylistPlayerStatus.IDLE -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (status) {
        PlaylistPlayerStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        PlaylistPlayerStatus.PLAYING -> MaterialTheme.colorScheme.onPrimaryContainer
        PlaylistPlayerStatus.BUFFERING -> MaterialTheme.colorScheme.onSecondaryContainer
        PlaylistPlayerStatus.ENDED -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun playbackStatusDescription(
    status: PlaylistPlayerStatus,
    errorMessage: String?,
): String {
    return when (status) {
        PlaylistPlayerStatus.BUFFERING -> "正在连接音源"
        PlaylistPlayerStatus.PLAYING -> "当前歌曲正在播放"
        PlaylistPlayerStatus.PAUSED -> "播放已暂停"
        PlaylistPlayerStatus.ENDED -> "当前歌曲已播完"
        PlaylistPlayerStatus.ERROR -> errorMessage ?: "播放失败"
        PlaylistPlayerStatus.IDLE -> "从列表挑一首开始试听"
    }
}

private fun compactPlaybackStatusText(
    status: PlaylistPlayerStatus,
    errorMessage: String?,
): String {
    return when (status) {
        PlaylistPlayerStatus.BUFFERING -> "缓冲中"
        PlaylistPlayerStatus.PLAYING -> "播放中"
        PlaylistPlayerStatus.PAUSED -> "已暂停"
        PlaylistPlayerStatus.ENDED -> "可重播"
        PlaylistPlayerStatus.ERROR -> errorMessage ?: "播放失败"
        PlaylistPlayerStatus.IDLE -> "等待播放"
    }
}

@Composable
private fun PlaylistTransportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Boolean,
) {
    OutlinedIconButton(
        onClick = { onClick() },
        enabled = enabled,
        modifier = Modifier.size(42.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PlaylistVolumeRow(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
) {
    val safeVolume = volume.coerceIn(0f, 1f)
    var lastAudibleVolume by remember { mutableStateOf(safeVolume.takeIf { it > 0.05f } ?: 0.72f) }
    if (safeVolume > 0.05f) {
        lastAudibleVolume = safeVolume
    }
    val isMuted = safeVolume <= 0.001f
    val volumeIcon = when {
        isMuted -> Icons.AutoMirrored.Filled.VolumeMute
        safeVolume < 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
        else -> Icons.AutoMirrored.Filled.VolumeUp
    }
    val volumePercentage = "${(safeVolume * 100).toInt()}%"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "音量",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isMuted) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    },
                ) {
                    Text(
                        text = volumePercentage,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isMuted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isMuted) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    },
                ) {
                    IconButton(
                        onClick = {
                            if (isMuted) {
                                onVolumeChange(lastAudibleVolume.coerceIn(0.35f, 1f))
                            } else {
                                onVolumeChange(0f)
                            }
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = volumeIcon,
                            contentDescription = if (isMuted) "取消静音" else "静音",
                            tint = if (isMuted) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    modifier = Modifier.size(18.dp),
                )

                Slider(
                    value = safeVolume,
                    onValueChange = { nextVolume ->
                        if (nextVolume > 0.05f) {
                            lastAudibleVolume = nextVolume
                        }
                        onVolumeChange(nextVolume)
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = CircleShape,
                                ),
                        )
                    },
                    track = { _ ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(safeVolume)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                    },
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (isMuted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
                    },
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaylistLyricsCard(
    lyrics: ParsedPlaylistLyrics?,
    loading: Boolean,
    error: String?,
    playbackProgress: PlaylistPlaybackProgress,
    emptyHint: String,
) {
    val listState = rememberLazyListState()
    val activeIndex = remember(lyrics, playbackProgress.positionMs, playbackProgress.playbackKey) {
        if (playbackProgress.playbackKey == null) {
            null
        } else {
            lyrics?.activeIndex(playbackProgress.positionMs)
        }
    }

    LaunchedEffect(activeIndex, lyrics?.raw) {
        val targetIndex = activeIndex ?: return@LaunchedEffect
        val scrollIndex = (targetIndex - 2).coerceAtLeast(0)
        listState.animateScrollToItem(scrollIndex)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "实时歌词",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )

            when {
                loading -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Text(
                            text = "正在加载歌词...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                error != null -> {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                lyrics == null || lyrics.lines.isEmpty() -> {
                    Text(
                        text = emptyHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(
                            items = lyrics.lines,
                            key = { index, line -> "${line.timeMs ?: -1L}:${line.text}:$index" },
                        ) { index, line ->
                            val isActive = index == activeIndex
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isActive) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                                },
                            ) {
                                Text(
                                    text = line.text,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = if (isActive) {
                                        MaterialTheme.typography.bodyLarge
                                    } else {
                                        MaterialTheme.typography.bodyMedium
                                    },
                                    color = if (isActive) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultLegacyPlayerContent(
    state: PlaylistPlaybackState<*>,
) {
    when {
        state.isResolving -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                Text(
                    text = "正在加载音频...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        state.selectedItem != null && state.resolvedUrl != null -> {
            Text(
                text = "兼容模式下已切换到内置控制器，请直接使用 DefaultPlaylistPlayer 获得完整播放体验。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        else -> {
            Text(
                text = state.emptyHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    state.resolveError?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun PlaylistPlayerHeader(
    title: String,
    subtitle: String?,
    coverUrl: String?,
    durationMs: Long?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistCover(
            coverUrl = coverUrl,
            size = 112.dp,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "播放器",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = title.ifBlank { "未命名音轨" },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        durationMs?.takeIf { it > 0L }?.let {
            PlaylistMetadataPill(text = formatPlaylistDuration(it))
        }
    }
}

@Composable
private fun PlaylistListItem(
    title: String,
    subtitle: String,
    durationMs: Long?,
    coverUrl: String?,
    state: PlaylistItemUiState,
    onPlayClick: () -> Unit,
    itemActions: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !state.isUnavailable && !state.isResolving,
                onClick = onPlayClick,
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (state.isCurrent) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (state.isCurrent) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
            },
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val compactLayout = maxWidth < 760.dp

            if (compactLayout) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PlaylistQueueIdentity(
                        title = title,
                        subtitle = subtitle,
                        durationMs = durationMs,
                        coverUrl = coverUrl,
                        state = state,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                itemActions()
                                FilledTonalButton(
                                    onClick = onPlayClick,
                                    enabled = !state.isUnavailable && !state.isResolving,
                                ) {
                                    Text(state.buttonLabel)
                                }
                            },
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlaylistQueueIdentity(
                        title = title,
                        subtitle = subtitle,
                        durationMs = durationMs,
                        coverUrl = coverUrl,
                        state = state,
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                            itemActions()
                            FilledTonalButton(
                                onClick = onPlayClick,
                                enabled = !state.isUnavailable && !state.isResolving,
                            ) {
                                Text(state.buttonLabel)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistQueueIdentity(
    title: String,
    subtitle: String,
    durationMs: Long?,
    coverUrl: String?,
    state: PlaylistItemUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = if (state.isCurrent) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when {
                        state.isPlaying || state.isBuffering -> Icons.Filled.GraphicEq
                        state.isEnded -> Icons.Filled.Replay
                        else -> Icons.Filled.PlayArrow
                    },
                    contentDescription = null,
                    tint = if (state.isCurrent) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }

        PlaylistCover(
            coverUrl = coverUrl,
            size = 56.dp,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title.ifBlank { "未命名音轨" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PlaylistItemStatusText(state)
        }

        durationMs?.takeIf { it > 0L }?.let {
            PlaylistMetadataPill(text = formatPlaylistDuration(it))
        }
    }
}

@Composable
private fun PlaylistItemStatusText(state: PlaylistItemUiState) {
    state.statusLabel?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.labelMedium,
            color = if (state.isUnavailable) {
                MaterialTheme.colorScheme.error
            } else if (state.isCurrent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun PlaylistCover(
    coverUrl: String?,
    size: Dp,
) {
    if (!coverUrl.isNullOrBlank()) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop,
        )
        return
    }

    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "♪",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PlaylistMetadataPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private data class PlaylistUrlDialogState(
    val title: String,
    val subtitle: String,
    val playbackId: String,
    val url: String,
)

@Composable
private fun PlaylistUrlDialog(
    state: PlaylistUrlDialogState,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.title.ifBlank { "歌曲 URL" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                if (state.subtitle.isNotBlank()) {
                    Text(
                        text = state.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PlaylistMetadataPill(text = state.playbackId)
                SelectionContainer {
                    Text(
                        text = state.url,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onCopy) {
                Text("复制 URL")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

internal fun formatPlaylistDuration(durationMs: Long): String {
    if (durationMs <= 0L) {
        return "0:00"
    }

    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
