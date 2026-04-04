package site.addzero.themes

import androidx.compose.ui.graphics.Color

/**
 * 深色主题颜色配置，实现了 [ShadcnColors] 接口
 *
 * 定义了深色模式下所有组件使用的颜色常量，包括背景、前景、卡片、
 * 弹出框、主要、次要、静音、强调、危险、边框、输入框、环形、图表、
 * 侧边栏和通知栏等颜色。
 */
object DarkColors : ShadcnColors {
    override val background = Color(0xFF0A0A0A)
    override val foreground = Color(0xFFFAFAFA)
    override val card = Color(0xFF171717)
    override val cardForeground = Color(0xFFFAFAFA)
    override val popover = Color(0xFF262626)
    override val popoverForeground = Color(0xFFFAFAFA)
    override val primary = Color(0xFFE5E5E5)
    override val primaryForeground = Color(0xFF171717)
    override val secondary = Color(0xFF262626)
    override val secondaryForeground = Color(0xFFFAFAFA)
    override val muted = Color(0xFF262626)
    override val mutedForeground = Color(0xFFA1A1A1)
    override val accent = Color(0xFF404040)
    override val accentForeground = Color(0xFFFAFAFA)
    override val destructive = Color(0xFFFF6467)
    override val destructiveForeground = Color(0xFFFAFAFA)
    override val border = Color(0xFF282828)
    override val input = Color(0xFF343434)
    override val ring = Color(0xFF737373)

    override val chart1 = Color(0xFF91C5FF)
    override val chart2 = Color(0xFF3A81F6)
    override val chart3 = Color(0xFF2563EF)
    override val chart4 = Color(0xFF1A4EDA)
    override val chart5 = Color(0xFF1F3FAD)

    override val sidebar = Color(0xFF171717)
    override val sidebarForeground = Color(0xFFFAFAFA)
    override val sidebarPrimary = Color(0xFF1447E6) // 已更新
    override val sidebarPrimaryForeground = Color(0xFFFAFAFA)
    override val sidebarAccent = Color(0xFF262626)
    override val sidebarAccentForeground = Color(0xFFFAFAFA)
    override val sidebarBorder = Color(0xFF282828)
    override val sidebarRing = Color(0xFF525252)
    override val snackbar = Color(0xFF262626)
}
