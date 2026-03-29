package site.addzero.component.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GlassIconButton — 玻璃风格图标按钮
 *
 * 使用 [glassEffect] 渲染半透明玻璃质感的圆形图标按钮。
 * 适用于播放控制、工具栏等场景。
 *
 * @param onClick 点击回调
 * @param modifier 外部修饰符
 * @param enabled 是否启用，默认 true
 * @param size 按钮尺寸，默认 40dp
 * @param shape 按钮形状，默认圆形
 * @param content 图标内容 slot
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.4f)
            .glassEffect(shape = shape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * NeonGlassIconButton — 霓虹发光图标按钮
 *
 * 使用 [neonGlassEffect] 渲染带霓虹发光边框的圆形图标按钮。
 * 适用于主要操作按钮（如播放按钮）。
 *
 * @param onClick 点击回调
 * @param modifier 外部修饰符
 * @param glowColor 霓虹发光颜色，默认 [GlassTheme.NeonCyan]
 * @param intensity 发光强度 (0.0 ~ 1.0)，默认 0.6
 * @param enabled 是否启用，默认 true
 * @param size 按钮尺寸，默认 48dp
 * @param shape 按钮形状，默认圆形
 * @param content 图标内容 slot
 */
@Composable
fun NeonGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = GlassTheme.NeonCyan,
    intensity: Float = 0.6f,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.4f)
            .neonGlassEffect(
                shape = shape,
                glowColor = glowColor,
                intensity = intensity,
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * LiquidGlassIconButton — 液态玻璃图标按钮
 *
 * 使用 [liquidGlassEffect] 渲染多层渐变和光折射边框高光的图标按钮。
 *
 * @param onClick 点击回调
 * @param modifier 外部修饰符
 * @param primaryColor 主表面色，默认 [GlassTheme.WaterSurfacePrimary]
 * @param secondaryColor 次表面色，默认 [GlassTheme.WaterSurfaceSecondary]
 * @param enabled 是否启用，默认 true
 * @param size 按钮尺寸，默认 56dp
 * @param shape 按钮形状，默认圆形
 * @param content 图标内容 slot
 */
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = GlassTheme.WaterSurfacePrimary,
    secondaryColor: Color = GlassTheme.WaterSurfaceSecondary,
    enabled: Boolean = true,
    size: Dp = 56.dp,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.4f)
            .liquidGlassEffect(
                shape = shape,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
