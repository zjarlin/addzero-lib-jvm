package site.addzero.appsidebar.spi

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 侧栏拖拽分隔条的视觉配置。
 */
interface SidebarResizeConfig {
    val dividerColor: Color
    val thumbColor: Color
    val thumbBorderColor: Color
}

/**
 * 创建侧栏拖拽分隔条配置。
 */
fun sidebarResizeConfig(
    dividerColor: Color,
    thumbColor: Color,
    thumbBorderColor: Color,
): SidebarResizeConfig = DefaultSidebarResizeConfig(
    dividerColor = dividerColor,
    thumbColor = thumbColor,
    thumbBorderColor = thumbBorderColor,
)

@Immutable
private data class DefaultSidebarResizeConfig(
    override val dividerColor: Color,
    override val thumbColor: Color,
    override val thumbBorderColor: Color,
) : SidebarResizeConfig
