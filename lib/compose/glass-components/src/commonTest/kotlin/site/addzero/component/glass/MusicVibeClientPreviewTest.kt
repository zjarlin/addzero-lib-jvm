package site.addzero.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.kyant.shapes.RoundedRectangle
import kotlin.test.Test

/**
 * Music Vibe Client 预览测试
 *
 * 使用 [runComposeUiTest] 构建类似效果图的完整音乐播放器 UI 预览，
 * 验证所有 Glass 组件能正确组合渲染。
 */
class MusicVibeClientPreviewTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun musicVibeClientPreview() = runComposeUiTest {
        setContent {
            MusicVibeClientApp()
        }

        // 验证关键 UI 节点存在
        onNodeWithText("MUSIC VIBE").assertExists()
        onNodeWithText("CLIENT").assertExists()
        onNodeWithText("Now Playing").assertExists()
        onNodeWithText("Midnight Drift").assertExists()
        onNodeWithText("Artist: Neon Dreams").assertExists()
        onNodeWithText("UPCOMING TRACKS").assertExists()
        onNodeWithTag("sidebar").assertExists()
        onNodeWithTag("now-playing").assertExists()
        onNodeWithTag("upcoming-tracks").assertExists()
        onNodeWithTag("play-progress").assertExists()
        onNodeWithTag("volume-slider").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun sidebarNavigationItemsExist() = runComposeUiTest {
        setContent {
            MusicVibeClientApp()
        }
        onNodeWithText("Home").assertExists()
        onNodeWithText("Library").assertExists()
        onNodeWithText("Playlists").assertExists()
        onNodeWithText("Explore").assertExists()
        onNodeWithText("Settings").assertExists()
        onNodeWithText("Profile").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun upcomingTracksListExist() = runComposeUiTest {
        setContent {
            MusicVibeClientApp()
        }
        onNodeWithText("Starlight Serenade").assertExists()
        onNodeWithText("Urban Pulse").assertExists()
        onNodeWithText("Ocean Drive").assertExists()
        onNodeWithText("Dreamscape").assertExists()
    }
}

// ── 预览 App ─────────────────────────────────────────────────

@Composable
private fun MusicVibeClientApp() {
    // 深色渐变背景（模拟效果图的紫/青背景光晕）
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2A1B4E),
                        Color(0xFF150D2E),
                        GlassTheme.DarkBackground,
                    )
                )
            )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // ── 左侧侧边栏 ──
            SidebarPanel(
                modifier = Modifier
                    .testTag("sidebar")
                    .width(200.dp)
                    .fillMaxHeight()
            )

            // ── 中间主区域 ──
            NowPlayingPanel(
                modifier = Modifier
                    .testTag("now-playing")
                    .weight(1f)
                    .fillMaxHeight()
            )

            // ── 右侧轨道列表 + 音量 ──
            RightPanel(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
            )
        }
    }
}

// ── 侧边栏 ──────────────────────────────────────────────────

@Composable
private fun SidebarPanel(modifier: Modifier = Modifier) {
    var selectedItem by remember { mutableStateOf("Playlists") }

    Column(
        modifier = modifier
            .glassEffect(shape = RoundedRectangle(0.dp))
            .padding(vertical = 20.dp, horizontal = 12.dp),
    ) {
        // Logo
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            Text(
                text = "MUSIC VIBE",
                color = GlassTheme.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "CLIENT",
                color = GlassTheme.NeonCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 导航项
        SidebarNavItem("Home", Icons.Default.Home, selectedItem == "Home") { selectedItem = "Home" }
        SidebarNavItem("Library", Icons.Default.MusicNote, selectedItem == "Library") { selectedItem = "Library" }
        SidebarNavItem("Playlists", Icons.Default.QueueMusic, selectedItem == "Playlists") { selectedItem = "Playlists" }
        SidebarNavItem("Explore", Icons.Default.Language, selectedItem == "Explore") { selectedItem = "Explore" }
        SidebarNavItem("Settings", Icons.Default.Settings, selectedItem == "Settings") { selectedItem = "Settings" }

        Spacer(modifier = Modifier.weight(1f))

        SidebarNavItem("Profile", Icons.Default.Person, selectedItem == "Profile") { selectedItem = "Profile" }
    }
}

@Composable
private fun SidebarNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) GlassTheme.NeonCyan.copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (isSelected) GlassTheme.NeonCyan else GlassTheme.TextSecondary
    val iconColor = if (isSelected) GlassTheme.NeonCyan else GlassTheme.TextTertiary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(bgColor, shape = RoundedRectangle(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

// ── Now Playing 面板 ─────────────────────────────────────────

@Composable
private fun NowPlayingPanel(modifier: Modifier = Modifier) {
    var progress by remember { mutableStateOf(0.3f) }

    Column(
        modifier = modifier.padding(24.dp),
    ) {
        // 顶栏：标题 + 搜索
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Now Playing",
                color = GlassTheme.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            GlassSearchField(
                value = "",
                onValueChange = {},
                onSearch = {},
                modifier = Modifier.width(180.dp),
                placeholder = "Search",
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 波形可视化区域（占位）
        NeonGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            glowColor = GlassTheme.NeonPurple,
            intensity = 0.4f,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "♪ Waveform Visualizer ♪",
                    color = GlassTheme.NeonCyan.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 播放控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(onClick = {}, size = 36.dp) {
                Icon(Icons.Default.Shuffle, "Shuffle", tint = GlassTheme.TextSecondary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            GlassIconButton(onClick = {}, size = 40.dp) {
                Icon(Icons.Default.SkipPrevious, "Previous", tint = GlassTheme.TextPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            NeonGlassIconButton(
                onClick = {},
                size = 56.dp,
                glowColor = GlassTheme.NeonPurple,
                intensity = 0.8f,
            ) {
                Icon(Icons.Default.PlayArrow, "Play", tint = GlassTheme.TextPrimary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            GlassIconButton(onClick = {}, size = 40.dp) {
                Icon(Icons.Default.SkipNext, "Next", tint = GlassTheme.TextPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            GlassIconButton(onClick = {}, size = 36.dp) {
                Icon(Icons.Default.Repeat, "Repeat", tint = GlassTheme.TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 播放进度条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("play-progress"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("1:24", color = GlassTheme.TextTertiary, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            GlassSlider(
                value = progress,
                onValueChange = { progress = it },
                modifier = Modifier.weight(1f),
                accentColor = GlassTheme.NeonCyan,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("4:55", color = GlassTheme.TextTertiary, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 曲目信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 封面占位
            GlassCard(
                modifier = Modifier.size(64.dp),
                shape = RoundedRectangle(12.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🌃", fontSize = 28.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Midnight Drift",
                    color = GlassTheme.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Artist: Neon Dreams",
                    color = GlassTheme.TextSecondary,
                    fontSize = 14.sp,
                )
                Text(
                    text = "Album: Ethereal Beats",
                    color = GlassTheme.TextTertiary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

// ── 右侧面板 ────────────────────────────────────────────────

@Composable
private fun RightPanel(modifier: Modifier = Modifier) {
    var volume by remember { mutableStateOf(0.65f) }

    Column(
        modifier = modifier.padding(16.dp),
    ) {
        // Upcoming Tracks 面板
        GlassCard(
            modifier = Modifier
                .testTag("upcoming-tracks")
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedRectangle(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "UPCOMING TRACKS",
                    color = GlassTheme.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Currently playing",
                    color = GlassTheme.TextTertiary,
                    fontSize = 11.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val scrollState = rememberScrollState()
                Row {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        TrackItem("2", "Starlight Serenade", "Echo & The Light", isPlaying = true)
                        TrackItem("3", "Urban Pulse", "Beat Architect", duration = "2:23")
                        TrackItem("4", "Ocean Drive", "Synthwave Collective", duration = "4:33")
                        TrackItem("5", "Dreamscape", "The Vibe Tribe", duration = "5:17")
                    }
                    // 滚动条
                    GlassScrollbar(
                        scrollState = scrollState,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 4.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 音量控制
        Row(
            modifier = Modifier
                .testTag("volume-slider")
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.VolumeUp,
                contentDescription = "Volume",
                tint = GlassTheme.TextTertiary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            GlassSlider(
                value = volume,
                onValueChange = { volume = it },
                modifier = Modifier.weight(1f),
                accentColor = GlassTheme.NeonPurple,
            )
        }
    }
}

@Composable
private fun TrackItem(
    number: String,
    title: String,
    artist: String,
    isPlaying: Boolean = false,
    duration: String? = null,
) {
    GlassListItem(
        title = "$number. $title",
        subtitle = artist,
        isSelected = isPlaying,
        leading = {
            // 封面占位
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GlassTheme.DarkSurface),
                contentAlignment = Alignment.Center,
            ) {
                Text("🎵", fontSize = 16.sp)
            }
        },
        trailing = {
            if (isPlaying) {
                Text("▶", color = GlassTheme.NeonCyan, fontSize = 12.sp)
            } else if (duration != null) {
                Text(duration, color = GlassTheme.TextTertiary, fontSize = 12.sp)
            }
        },
        thumbnailSize = 36.dp,
    )
}
