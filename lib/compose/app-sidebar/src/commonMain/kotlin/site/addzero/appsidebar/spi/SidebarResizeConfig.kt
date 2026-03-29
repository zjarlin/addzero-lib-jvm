package site.addzero.appsidebar.spi

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

interface SidebarResizeConfig {
    val dividerColor: Color
    val thumbColor: Color
    val thumbBorderColor: Color
}

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
