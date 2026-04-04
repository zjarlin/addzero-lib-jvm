package site.addzero.component.glass

import androidx.compose.ui.graphics.Color

/**
 * GlassColors — 向后兼容的颜色别名
 *
 * 将旧版 GlassColors 的颜色常量映射到新版 [GlassTheme] 的对应 Token，
 * 确保现有代码无需修改即可编译通过。
 *
 * 新代码应优先使用 [GlassTheme]。
 */
object GlassColors {

    /** 玻璃表面色 — 等同于 [GlassTheme.GlassSurface] */
    val Surface get() = GlassTheme.GlassSurface

    /** 玻璃边框色 — 等同于 [GlassTheme.GlassBorder] */
    val Border get() = GlassTheme.GlassBorder

    /** 玻璃阴影色 — 等同于 [GlassTheme.GlassShadow] */
    val Shadow get() = GlassTheme.GlassShadow

    // ── 霓虹色彩 ──

    /** 霓虹青色 — 等同于 [GlassTheme.NeonCyan] */
    val NeonCyan get() = GlassTheme.NeonCyan

    /** 霓虹紫色 — 等同于 [GlassTheme.NeonPurple] */
    val NeonPurple get() = GlassTheme.NeonPurple

    /** 霓虹品红 — 等同于 [GlassTheme.NeonMagenta] */
    val NeonMagenta get() = GlassTheme.NeonMagenta

    /** 霓虹粉色 — 等同于 [GlassTheme.NeonPink] */
    val NeonPink get() = GlassTheme.NeonPink

    // ── 背景色 ──

    /** 深色背景 — 等同于 [GlassTheme.DarkBackground] */
    val DarkBackground get() = GlassTheme.DarkBackground

    /** 深色表面 — 等同于 [GlassTheme.DarkSurface] */
    val DarkSurface get() = GlassTheme.DarkSurface
}
