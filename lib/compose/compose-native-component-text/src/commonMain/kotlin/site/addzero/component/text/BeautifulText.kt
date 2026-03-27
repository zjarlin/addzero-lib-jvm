package site.addzero.component.text

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 🎨 独立的美化标题组件
 * @param text 标题文本
 * @param useGradientEffect 是否使用渐变效果，默认 true
 * @param textStyle 文本样式，可自定义
 * @param iconSize 图标大小，默认 20.dp
 * @param spacing 图标与文本间距，默认 8.dp
 * @param backgroundColor 背景颜色（渐变效果时使用）
 * @param textColor 文本颜色
 * @param leftIcon 左侧图标，默认 Icons.Default.Star
 * @param rightIcon 右侧图标，默认 Icons.Default.Star
 * @param leftIconColor 左侧图标颜色
 * @param rightIconColor 右侧图标颜色
 * @param showLeftIcon 是否显示左侧图标，默认 true
 * @param showRightIcon 是否显示右侧图标，默认 true
 */
@Composable
fun BeautifulText(
    text: String,
    useGradientEffect: Boolean = true,
    textStyle: TextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    iconSize: Dp = 20.dp,
    spacing: Dp = 8.dp,
    backgroundColor: Color = Color(0xFF6200EE),
    textColor: Color = Color.White,
    leftIcon: ImageVector = Icons.Default.Star,
    rightIcon: ImageVector = Icons.Default.Star,
    leftIconColor: Color = Color(0xFF03DAC6),
    rightIconColor: Color = Color(0xFF03DAC6),
    showLeftIcon: Boolean = true,
    showRightIcon: Boolean = true
) {
    // 🌟 动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "title_animation")

    // 缩放动画
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // 渐变动画（仅在启用渐变效果时使用）
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_animation"
    )

    // 计算动态颜色
    val dynamicLeftIconColor = remember(useGradientEffect, gradientOffset, leftIconColor) {
        if (useGradientEffect) {
            Color.hsl(
                hue = (gradientOffset * 360f) % 360f,
                saturation = 0.8f,
                lightness = 0.6f
            )
        } else {
            leftIconColor
        }
    }

    val dynamicRightIconColor = remember(useGradientEffect, gradientOffset, rightIconColor) {
        if (useGradientEffect) {
            Color.hsl(
                hue = ((gradientOffset * 360f) + 180f) % 360f,
                saturation = 0.8f,
                lightness = 0.6f
            )
        } else {
            rightIconColor
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.scale(scale)
    ) {
        // 🌟 左侧装饰性图标
        if (showLeftIcon) {
            Icon(
                imageVector = leftIcon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = dynamicLeftIconColor
            )
            Spacer(modifier = Modifier.width(spacing))
        }

        if (useGradientEffect) {
            // 🎨 渐变背景效果
            val gradientColors = remember(gradientOffset) {
                listOf(
                    Color.hsl(
                        hue = (gradientOffset * 360f) % 360f,
                        saturation = 0.9f,
                        lightness = 0.7f,
                        alpha = 0.3f
                    ),
                    Color.hsl(
                        hue = ((gradientOffset * 360f) + 120f) % 360f,
                        saturation = 0.9f,
                        lightness = 0.7f,
                        alpha = 0.2f
                    ),
                    Color.hsl(
                        hue = ((gradientOffset * 360f) + 240f) % 360f,
                        saturation = 0.9f,
                        lightness = 0.7f,
                        alpha = 0.3f
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(colors = gradientColors)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = text,
                    style = textStyle,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // 📝 简洁文本样式
            Text(
                text = text,
                style = textStyle,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        if (showRightIcon) {
            Spacer(modifier = Modifier.width(spacing))
            // 🌟 右侧装饰性图标
            Icon(
                imageVector = rightIcon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = dynamicRightIconColor
            )
        }
    }
}
