package site.addzero.component.glass

import androidx.compose.ui.graphics.Color

/**
 * GlassTheme — 水玻璃设计系统的颜色与 Token 定义
 *
 * 提供统一的颜色调色板，包含深色背景、玻璃表面、霓虹强调色和文字颜色。
 * 所有颜色值与现有 GlassColors 保持兼容，同时扩展了更完整的 Token 体系。
 *
 * @see GlassColors 向后兼容的颜色别名
 */
object GlassTheme {

    // ── 背景层 ──────────────────────────────────────────────
    /** 最深层背景色，用于应用主背景 */
    val DarkBackground = Color(0xFFF4F8FF)

    /** 次级深色表面，用于卡片/面板底层（保证 Desktop JVM 无模糊时的文字可读性） */
    val DarkSurface = Color(0xFFFFFFFF)

    // ── 玻璃表面 ────────────────────────────────────────────
    /** 玻璃表面色 — 10% 白色半透明 */
    val GlassSurface = Color(0xFFF7FAFF)

    /** 玻璃表面悬停态 — 15% 白色半透明 */
    val GlassSurfaceHover = Color(0xFFEFF4FF)

    /** 玻璃边框色 — 25% 白色半透明 */
    val GlassBorder = Color(0xFFD3E0F3)

    /** 玻璃阴影色 */
    val GlassShadow = Color(0x140F172A)

    // ── 无色水玻璃 Token ───────────────────────────────────
    /** 无色液态玻璃主表面（中性白） */
    val WaterSurfacePrimary = Color(0xFFF7FAFF)

    /** 无色液态玻璃次表面（更浅） */
    val WaterSurfaceSecondary = Color(0xFFEFF4FF)

    /** 边缘折射高光（强） */
    val WaterRefractionEdgeStrong = Color(0xFF2563EB)

    /** 边缘折射高光（弱） */
    val WaterRefractionEdgeSoft = Color(0xFF93C5FD)

    /** 内缘折射线（用于双层边缘） */
    val WaterRefractionInner = Color(0xFFDCE8FF)

    // ── JetBrains 紫色系 ────────────────────────────────────
    /** JetBrains 主紫色 */
    val JBPurple = Color(0xFF2563EB)

    /** JetBrains 深紫底色（侧边栏底层） */
    val JBPurpleDark = Color(0xFFE0EAFF)

    /** JetBrains 紫色表面（半透明叠加层） */
    val JBPurpleSurface = Color(0xFFDBEAFE)

    /** JetBrains 紫色选中高亮 */
    val JBPurpleHighlight = Color(0xFF60A5FA)

    // ── 霓虹强调色 ──────────────────────────────────────────
    /** 霓虹青色 */
    val NeonCyan = Color(0xFF2563EB)

    /** 霓虹紫色 */
    val NeonPurple = Color(0xFF1D4ED8)

    /** 霓虹品红 */
    val NeonMagenta = Color(0xFFDC2626)

    /** 霓虹粉色 */
    val NeonPink = Color(0xFF0EA5E9)

    // ── 文字颜色 ────────────────────────────────────────────
    /** 主要文字 — 纯白 */
    val TextPrimary: Color = Color(0xFF162033)

    /** 次要文字 — 80% 白 */
    val TextSecondary: Color = Color(0xFF3E4C67)

    /** 三级文字 — 60% 白 */
    val TextTertiary: Color = Color(0xFF66758F)

    /** 禁用态文字 — 40% 白 */
    val TextDisabled: Color = Color(0xFF94A3B8)
}
