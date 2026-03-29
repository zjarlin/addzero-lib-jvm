package site.addzero.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.kyant.shapes.RoundedRectangle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GlassEffect — 水玻璃效果核心 Modifier 扩展
 *
 * 提供三种玻璃效果 Modifier：
 * - [glassEffect] — 基础玻璃（深色底层 + 半透明线性渐变 + 玻璃边框）
 * - [neonGlassEffect] — 霓虹发光玻璃（深色底层 + 径向发光渐变 + 霓虹色边框）
 * - [liquidGlassEffect] — 液态玻璃（深色底层 + 多层渐变 + 光折射风格边框高光）
 *
 * 所有效果在 Desktop JVM 上通过 [GlassTheme.DarkSurface] 深色底层 + 半透明渐变叠加实现，
 * 无需原生模糊支持，确保文字可读性。
 *
 * 设计对齐 Apple Liquid Glass 设计语言的视觉风格。
 */

// ── 基础玻璃效果 ────────────────────────────────────────────

/**
 * 基础玻璃效果修饰符。
 *
 * 在深色底层 ([GlassTheme.DarkSurface]) 上叠加半透明线性渐变，
 * 并添加玻璃风格的半透明边框，模拟毛玻璃质感。
 *
 * @param shape 裁剪形状，默认 16dp 圆角
 * @param backgroundColor 玻璃表面色，默认 [GlassTheme.GlassSurface]（10% 白色）
 * @param borderColor 边框色，默认 [GlassTheme.GlassBorder]（25% 白色）
 * @param borderWidth 边框宽度，默认 1dp
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedRectangle(16.dp),
    backgroundColor: Color = GlassTheme.GlassSurface,
    borderColor: Color = GlassTheme.GlassBorder,
    borderWidth: Dp = 1.dp,
): Modifier {
    return this
        .clip(shape)
        // 深色底层 — 保证 Desktop JVM 无原生模糊时文字可读
        .background(GlassTheme.DarkSurface)
        // 半透明渐变叠加 — 模拟玻璃表面（降低 alpha 保证文字对比度）
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    backgroundColor.copy(alpha = 0.35f),
                    backgroundColor.copy(alpha = 0.15f),
                )
            )
        )
        // 玻璃边框 — 半透明渐变模拟光线折射
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    borderColor.copy(alpha = 0.6f),
                    borderColor.copy(alpha = 0.15f),
                )
            ),
            shape = shape,
        )
}

// ── 霓虹发光玻璃效果 ────────────────────────────────────────

/**
 * 霓虹发光玻璃效果修饰符。
 *
 * 在深色底层上叠加径向发光渐变，并添加霓虹色发光边框，
 * 营造赛博朋克风格的发光玻璃质感。
 *
 * @param shape 裁剪形状，默认 16dp 圆角
 * @param glowColor 发光颜色，默认 [GlassTheme.NeonCyan]
 * @param intensity 发光强度 (0.0 ~ 1.0)，默认 0.6
 */
fun Modifier.neonGlassEffect(
    shape: Shape = RoundedRectangle(16.dp),
    glowColor: Color = GlassTheme.NeonCyan,
    intensity: Float = 0.6f,
): Modifier {
    return this
        .clip(shape)
        // 深色底层
        .background(GlassTheme.DarkSurface)
        // 径向发光渐变 — 从中心向外扩散的霓虹光晕（降低 alpha 保证文字对比度）
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.10f * intensity),
                    glowColor.copy(alpha = 0.03f * intensity),
                )
            )
        )
        // 霓虹发光边框
        .border(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.8f * intensity),
                    glowColor.copy(alpha = 0.25f * intensity),
                )
            ),
            shape = shape,
        )
}

// ── 液态玻璃效果 ────────────────────────────────────────────

/**
 * 液态玻璃效果修饰符。
 *
 * 对齐 macOS 26 Liquid Glass 设计语言：在深色底层上叠加无色半透明层，
 * 并强化边缘折射高光，营造“无色水玻璃”的折射质感。
 *
 * 实现细节：
 * 1. 深色底层 ([GlassTheme.DarkSurface]) 保证可读性
 * 2. 多层无色半透明叠加，模拟液体厚度
 * 3. 内部微光层 ([drawBehind])，模拟水体内的散射
 * 4. 双层边缘折射线（顶边/左边/右下边），强调光线折射
 *
 * @param shape 裁剪形状，默认 24dp 圆角（液态玻璃通常使用更大圆角）
 * @param primaryColor 主表面色，默认 [GlassTheme.WaterSurfacePrimary]
 * @param secondaryColor 次表面色，默认 [GlassTheme.WaterSurfaceSecondary]
 */
fun Modifier.liquidGlassEffect(
    shape: Shape = RoundedRectangle(24.dp),
    primaryColor: Color = GlassTheme.WaterSurfacePrimary,
    secondaryColor: Color = GlassTheme.WaterSurfaceSecondary,
): Modifier {
    return this
        .clip(shape)
        // 深色底层
        .background(GlassTheme.DarkSurface.copy(alpha = 0.92f))
        // 无色多层叠加 — 模拟水体厚度
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    primaryColor.copy(alpha = 0.85f),
                    secondaryColor.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.04f),
                )
            )
        )
        // 内部微光层 + 边缘折射线
        .drawBehind {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.22f, size.height * 0.12f),
                    radius = size.minDimension * 0.95f,
                )
            )

            val strong = GlassTheme.WaterRefractionEdgeStrong
            val soft = GlassTheme.WaterRefractionEdgeSoft
            val strokeTop = 1.6.dp.toPx()
            val strokeSide = 1.2.dp.toPx()

            // 顶边折射高光
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        strong,
                        soft,
                        Color.Transparent,
                    )
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = strokeTop,
            )

            // 左边折射高光
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        strong.copy(alpha = 0.9f),
                        soft,
                        Color.Transparent,
                    )
                ),
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = strokeSide,
            )

            // 右下边反向折射线
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        soft.copy(alpha = 0.8f),
                        strong.copy(alpha = 0.65f),
                    ),
                    start = Offset(size.width * 0.55f, size.height),
                    end = Offset(size.width, size.height * 0.55f),
                ),
                start = Offset(size.width * 0.55f, size.height),
                end = Offset(size.width, size.height * 0.55f),
                strokeWidth = strokeSide,
            )
        }
        // 主边缘折射
        .border(
            width = 1.4.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    GlassTheme.WaterRefractionEdgeStrong,
                    GlassTheme.WaterRefractionEdgeSoft,
                    Color.Transparent,
                )
            ),
            shape = shape,
        )
        // 内缘折射
        .border(
            width = 0.6.dp,
            color = GlassTheme.WaterRefractionInner,
            shape = shape,
        )
}


// ── JetBrains 紫色玻璃效果 ──────────────────────────────────

/**
 * JetBrains 风格紫色透明玻璃效果修饰符。
 *
 * 深紫底层 + 紫色半透明渐变叠加 + 微妙紫色边框，
 * 模拟 JetBrains IDE 侧边栏的紫色玻璃质感。
 *
 * @param shape 裁剪形状，默认无圆角（侧边栏全高）
 */
fun Modifier.jbPurpleGlassEffect(
    shape: Shape = RoundedRectangle(0.dp),
): Modifier {
    return this
        .clip(shape)
        // 深紫底层
        .background(GlassTheme.JBPurpleDark)
        // 紫色半透明渐变叠加 — 从上到下由浅紫渐变到深紫
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    GlassTheme.JBPurpleSurface.copy(alpha = 0.12f),
                    GlassTheme.JBPurple.copy(alpha = 0.06f),
                    Color.Transparent,
                )
            )
        )
        // 右侧紫色边框线 — 模拟侧边栏分隔
        .drawBehind {
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassTheme.JBPurpleHighlight.copy(alpha = 0.4f),
                        GlassTheme.JBPurple.copy(alpha = 0.15f),
                        Color.Transparent,
                    )
                ),
                start = Offset(size.width, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx(),
            )
        }
}
