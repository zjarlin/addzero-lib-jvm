package site.addzero.vibepocket.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 玻璃组件展示页面
 */
@Composable
fun GlassShowcase(
    modifier: Modifier = Modifier
) {
    var selectedSidebarItem by remember { mutableStateOf("home") }
    var searchText by remember { mutableStateOf("") }
    var textAreaValue by remember { mutableStateOf("") }
    
    // 侧边栏菜单项
    val sidebarItems = listOf(
        SidebarItem("home", "首页", Icons.Default.Home, isSelected = selectedSidebarItem == "home"),
        SidebarItem("music", "音乐", Icons.Default.MusicNote, "99+", isSelected = selectedSidebarItem == "music"),
        SidebarItem("playlist", "播放列表", Icons.Default.PlaylistPlay, isSelected = selectedSidebarItem == "playlist"),
        SidebarItem("favorites", "收藏", Icons.Default.Favorite, isSelected = selectedSidebarItem == "favorites"),
        SidebarItem("settings", "设置", Icons.Default.Settings, isSelected = selectedSidebarItem == "settings")
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlassColors.DarkBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // 侧边栏
            GlassSidebar(
                items = sidebarItems,
                onItemClick = { item ->
                    selectedSidebarItem = item.id
                },
                title = "VibePocket"
            )
            
            // 主内容区域
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    // 标题
                    Text(
                        text = "玻璃组件展示",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                
                item {
                    // 按钮展示
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlassInfoCard(
                            title = "按钮组件",
                            content = "各种风格的玻璃按钮效果"
                        )
                        
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        GlassButton(
                                            text = "基础按钮",
                                            onClick = { }
                                        )
                                    }
                                    item {
                                        NeonGlassButton(
                                            text = "霓虹按钮",
                                            onClick = { },
                                            glowColor = GlassColors.NeonCyan
                                        )
                                    }
                                    item {
                                        LiquidGlassButton(
                                            text = "液体按钮",
                                            onClick = { }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    // 输入框展示
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlassInfoCard(
                            title = "输入组件",
                            content = "玻璃风格的输入框和搜索框"
                        )
                        
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                GlassSearchField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    onSearch = { },
                                    placeholder = "搜索音乐、艺术家..."
                                )
                                
                                GlassTextArea(
                                    value = textAreaValue,
                                    onValueChange = { textAreaValue = it },
                                    placeholder = "输入你的想法...",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                item {
                    // 统计卡片展示
                    Text(
                        text = "统计卡片",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            listOf(
                                Triple("1,234", "总播放", GlassColors.NeonCyan),
                                Triple("567", "收藏歌曲", GlassColors.NeonPurple),
                                Triple("89", "播放列表", GlassColors.NeonMagenta),
                                Triple("12h", "今日聆听", GlassColors.NeonPink)
                            )
                        ) { (value, label, color) ->
                            GlassStatCard(
                                value = value,
                                label = label,
                                glowColor = color,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
                
                item {
                    // 功能卡片展示
                    Text(
                        text = "功能卡片",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassFeatureCard(
                                title = "智能推荐",
                                description = "基于你的听歌习惯，为你推荐更多好音乐",
                                modifier = Modifier.weight(1f),
                                primaryColor = GlassColors.NeonPurple,
                                secondaryColor = GlassColors.NeonCyan,
                                icon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GlassColors.NeonCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                            
                            GlassFeatureCard(
                                title = "高品质音频",
                                description = "支持无损音质播放，带来极致听觉体验",
                                modifier = Modifier.weight(1f),
                                primaryColor = GlassColors.NeonMagenta,
                                secondaryColor = GlassColors.NeonPink,
                                icon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.HighQuality,
                                        contentDescription = null,
                                        tint = GlassColors.NeonMagenta,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                        }
                        
                        GlassFeatureCard(
                            title = "跨平台同步",
                            description = "在所有设备上同步你的音乐库、播放列表和播放进度，随时随地享受音乐",
                            primaryColor = GlassColors.NeonCyan,
                            secondaryColor = GlassColors.NeonPurple,
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = GlassColors.NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )
                    }
                }
                
                item {
                    // 不同风格的卡片展示
                    Text(
                        text = "卡片样式",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "这是一个基础的玻璃卡片，具有半透明效果和柔和的边框。",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        NeonGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = GlassColors.NeonPurple
                        ) {
                            Text(
                                text = "这是一个霓虹玻璃卡片，带有发光的边框效果。",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "这是一个液体玻璃卡片，具有流动的渐变效果。",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}