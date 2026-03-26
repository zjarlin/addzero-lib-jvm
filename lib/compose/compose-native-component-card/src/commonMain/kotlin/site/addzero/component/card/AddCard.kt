package site.addzero.component.card

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

/**
 * 计算颜色的相对亮度
 * 根据 WCAG 2.0 标准计算
 */
private fun Color.luminance(): Float {
    fun linearize(component: Float): Float {
        return if (component <= 0.03928f) {
            component / 12.92f
        } else {
            ((component + 0.055f) / 1.055f).pow(2.4f)
        }
    }

    val r = linearize(red)
    val g = linearize(green)
    val b = linearize(blue)

    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

/**
 * 🎨 卡片组件
 * - 清晰的渐变背景
 * - 微妙的边框效果
 * - 流畅的悬浮动画
 * - 自动适配的文字颜色
 *
 * @param onClick 点击事件回调
 * @param modifier 修饰符
 * @param cornerRadius 圆角大小
 * @param elevation 阴影高度
 * @param padding 内边距
 * @param backgroundType 背景类型
 * @param animationDuration 动画持续时间
 * @param content 卡片内容插槽
 */
@Composable
fun AddCard(
    onClick: (() -> Unit) = {},
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 4.dp,
    padding: Dp = 20.dp,
    backgroundType: MellumCardType = adaptiveMellumCardType(),
    animationDuration: Int = 300,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()


    // 荧光色边框动画
    val glowAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.8f else 0f,
        animationSpec = tween(durationMillis = animationDuration, easing = EaseOutCubic),
        label = "glow_animation"
    )

    // 使用Box包装，确保荧光效果不影响卡片尺寸
    Box(
        modifier = modifier
    ) {
        // 荧光背景层，不影响卡片本身尺寸
        if (isHovered && glowAlpha > 0f) {
            Box(
                modifier = Modifier.matchParentSize().background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            backgroundType.hoverColor.copy(alpha = glowAlpha * 0.3f),
                            backgroundType.hoverColor.copy(alpha = glowAlpha * 0.1f),
                            Color.Transparent
                        ), radius = 200f
                    ), shape = RoundedCornerShape(cornerRadius + 8.dp)
                )
            )
        }

        // 主卡片，尺寸保持不变
        Surface(
            modifier = Modifier.then(
                Modifier.clickable(
                    interactionSource = interactionSource, indication = null
                ) { onClick() }
            ),
            shape = RoundedCornerShape(cornerRadius),
            tonalElevation = elevation,
            shadowElevation = elevation,
            color = backgroundType.backgroundColor) {
            // 直接使用Column布局，避免Box嵌套
            Column(
                modifier = Modifier.fillMaxWidth().background(
                    brush = backgroundType.backgroundBrush, shape = RoundedCornerShape(cornerRadius)
                )
                    // 荧光色边框效果
                    .border(
                        width = if (isHovered) 2.dp else 1.dp, brush = if (isHovered) {
                            Brush.linearGradient(
                                colors = listOf(
                                    backgroundType.hoverColor.copy(alpha = glowAlpha),
                                    backgroundType.hoverColor.copy(alpha = glowAlpha * 0.6f),
                                    backgroundType.borderColor.copy(alpha = 0.3f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    backgroundType.borderColor.copy(alpha = 0.2f),
                                    backgroundType.borderColor.copy(alpha = 0.1f)
                                )
                            )
                        }, shape = RoundedCornerShape(cornerRadius)
                    ).padding(padding)
            ) {
                // 提供LocalContentColor，确保文字颜色正确
                CompositionLocalProvider(
                    LocalContentColor provides backgroundType.contentColor
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * 🎨 Mellum卡片类型数据类
 *
 * 定义不同的背景渐变样式，参考JetBrains产品的配色方案
 */
enum class MellumCardType(
    val displayName: String,
    val backgroundBrush: Brush,
    val hoverColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
    val contentColor: Color
) {
    Light(
        displayName = "Light",
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC),
                Color(0xFFE2E8F0)
            )
        ),
        hoverColor = Color(0xFF3B82F6),
        backgroundColor = Color(0xFFFFFFFF),
        borderColor = Color(0xFFE2E8F0),
        contentColor = Color(0xFF1E293B)
    ),
    Purple(
        displayName = "Purple",
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF2D1B69), Color(0xFF1A0E3D), Color(0xFF0F0A1F)
            )
        ),
        hoverColor = Color(0xFF00D4FF),
        backgroundColor = Color(0xFF2D1B69),
        borderColor = Color(0xFF6B73FF),
        contentColor = Color(0xFFFFFFFF)
    ),
    Blue(
        displayName = "Blue",
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E3A8A), Color(0xFF1E293B), Color(0xFF0F172A)
            )
        ),
        hoverColor = Color(0xFF00FFFF),
        backgroundColor = Color(0xFF1E3A8A),
        borderColor = Color(0xFF3B82F6),
        contentColor = Color(0xFFFFFFFF)
    ),
    Teal(
        displayName = "Teal",
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF134E4A), Color(0xFF1F2937), Color(0xFF111827)
            )
        ),
        hoverColor = Color(0xFF00FF88),
        backgroundColor = Color(0xFF134E4A),
        borderColor = Color(0xFF14B8A6),
        contentColor = Color(0xFFFFFFFF)
    ),
    Orange(
        displayName = "Orange",
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF9A3412), Color(0xFF7C2D12), Color(0xFF431407)
            )
        ),
        hoverColor = Color(0xFFFF6600),
        backgroundColor = Color(0xFF9A3412),
        borderColor = Color(0xFFF97316),
        contentColor = Color(0xFFFFFFFF)
    ),
    Dark(
        displayName = "Dark",
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF374151), Color(0xFF1F2937), Color(0xFF111827)
            )
        ),
        hoverColor = Color(0xFFFFFFFF),
        backgroundColor = Color(0xFF374151),
        borderColor = Color(0xFF6B7280),
        contentColor = Color(0xFFFFFFFF)
    ),
    Rainbow(
        displayName = "Rainbow",
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF1F2937)
            )
        ),
        hoverColor = Color(0xFFFF00FF),
        backgroundColor = Color(0xFF8B5CF6),
        borderColor = Color(0xFF8B5CF6),
        contentColor = Color(0xFFFFFFFF)
    );

    companion object {
        fun fromName(name: String): MellumCardType? = entries.firstOrNull { it.displayName == name || it.name == name }
    }
}

/**
 * 🎨 自动适配系统主题的卡片类型工厂
 *
 * 根据当前 Material 3 主题自动选择合适的颜色
 * 必须在 @Composable 环境中调用
 */
@Composable
fun adaptiveMellumCardType(): MellumCardType {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.surface.luminance() < 0.5f

    return if (isDark) {
        MellumCardType.Dark
    } else {
        MellumCardType.Light
    }
}


