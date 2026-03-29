package site.addzero.component.glass

import com.kyant.shapes.RoundedRectangle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * GlassCard — 基础玻璃卡片容器
 *
 * 使用 [glassEffect] 渲染半透明玻璃质感，包含深色底层保证文字可读性、
 * 半透明渐变叠加和玻璃风格边框。
 *
 * @param modifier 外部修饰符
 * @param shape 卡片形状，默认 16dp 圆角
 * @param content 卡片内容
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedRectangle(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = GlassTheme.GlassSurface,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, GlassTheme.GlassBorder),
    ) {
        Box(content = content)
    }
}

/**
 * NeonGlassCard — 带霓虹发光边框的玻璃卡片
 *
 * 使用 [neonGlassEffect] 渲染径向发光渐变和霓虹色边框，
 * 营造赛博朋克风格的发光玻璃质感。
 *
 * @param modifier 外部修饰符
 * @param glowColor 霓虹发光颜色，默认 [GlassTheme.NeonCyan]
 * @param intensity 发光强度 (0.0 ~ 1.0)，默认 0.6
 * @param shape 卡片形状，默认 16dp 圆角
 * @param content 卡片内容
 */
@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = GlassTheme.NeonCyan,
    intensity: Float = 0.6f,
    shape: Shape = RoundedRectangle(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = glowColor.copy(alpha = 0.06f),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, glowColor.copy(alpha = 0.32f)),
    ) {
        Box(content = content)
    }
}

/**
 * LiquidGlassCard — 液态玻璃卡片
 *
 * 使用 [liquidGlassEffect] 渲染多层渐变和光折射边框高光，
 * 对齐 macOS 26 Liquid Glass 设计语言。
 *
 * @param modifier 外部修饰符
 * @param primaryColor 主表面色，默认 [GlassTheme.WaterSurfacePrimary]
 * @param secondaryColor 次表面色，默认 [GlassTheme.WaterSurfaceSecondary]
 * @param shape 卡片形状，默认 24dp 圆角
 * @param content 卡片内容
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    primaryColor: Color = GlassTheme.WaterSurfacePrimary,
    secondaryColor: Color = GlassTheme.WaterSurfaceSecondary,
    shape: Shape = RoundedRectangle(24.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = primaryColor,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, secondaryColor),
    ) {
        Box(content = content)
    }
}
