package site.addzero.themes

import androidx.compose.ui.graphics.Color

/**
 * 浅色主题颜色配置，实现了 [ShadcnColors] 接口
 *
 * 定义了浅色模式下所有组件使用的颜色常量，包括背景、前景、卡片、
 * 弹出框、主要、次要、静音、强调、危险、边框、输入框、环形、图表、
 * 侧边栏和通知栏等颜色。
 */
object LightColors : ShadcnColors {
    override val background = Color(0xFFFFFFFF)
    override val foreground = Color(0xFF0A0A0A)
    override val card = Color(0xFFFFFFFF)
    override val cardForeground = Color(0xFF0A0A0A)
    override val popover = Color(0xFFFFFFFF)
    override val popoverForeground = Color(0xFF0A0A0A)
    override val primary = Color(0xFF171717)
    override val primaryForeground = Color(0xFFFAFAFA)
    override val secondary = Color(0xFFF5F5F5)
    override val secondaryForeground = Color(0xFF171717)
    override val muted = Color(0xFFF5F5F5)
    override val mutedForeground = Color(0xFF737373)
    override val accent = Color(0xFFF5F5F5)
    override val accentForeground = Color(0xFF171717)
    override val destructive = Color(0xFFE7000B)
    override val destructiveForeground = Color(0xFFFFFFFF)
    override val border = Color(0xFFE5E5E5)
    override val input = Color(0xFFE5E5E5)
    override val ring = Color(0xFFA1A1A1)

    override val chart1 = Color(0xFFB2D4FF)
    override val chart2 = Color(0xFF3A81F6)
    override val chart3 = Color(0xFF2563EF)
    override val chart4 = Color(0xFF1A4EDA)
    override val chart5 = Color(0xFF1F3FAD)

    override val sidebar = Color(0xFFFAFAFA)
    override val sidebarForeground = Color(0xFF0A0A0A)
    override val sidebarPrimary = Color(0xFF171717)
    override val sidebarPrimaryForeground = Color(0xFFFAFAFA)
    override val sidebarAccent = Color(0xFFF5F5F5)
    override val sidebarAccentForeground = Color(0xFF171717)
    override val sidebarBorder = Color(0xFFE5E5E5)
    override val sidebarRing = Color(0xFFA1A1A1)
    override val snackbar = Color(0xFFFFFFFF)
}
