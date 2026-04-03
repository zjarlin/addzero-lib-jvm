package site.addzero.media.playlist.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * 播放器顶部的当前歌曲摘要区。
 */
@Composable
internal fun PlaylistPlayerHeader(
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

/**
 * 播放列表中的单条歌曲卡片。
 */
@Composable
internal fun PlaylistListItem(
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

/**
 * 队列项左侧的身份摘要区，统一显示封面、标题和播放状态。
 */
@Composable
internal fun PlaylistQueueIdentity(
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

/**
 * 队列项的状态标签。
 */
@Composable
internal fun PlaylistItemStatusText(state: PlaylistItemUiState) {
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

/**
 * 统一渲染歌曲封面，缺省时展示简洁占位。
 */
@Composable
internal fun PlaylistCover(
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

/**
 * 元信息药丸标签。
 */
@Composable
internal fun PlaylistMetadataPill(text: String) {
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

/**
 * URL 预览对话框的临时状态。
 */
internal data class PlaylistUrlDialogState(
    val title: String,
    val subtitle: String,
    val playbackId: String,
    val url: String,
)

/**
 * 展示单曲解析后 URL 的只读对话框。
 */
@Composable
internal fun PlaylistUrlDialog(
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
