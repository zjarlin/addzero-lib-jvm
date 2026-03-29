package site.addzero.component.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import com.kyant.shapes.RoundedRectangle
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * GlassStatCard — 统计数据展示卡片
 *
 * 使用 [neonGlassEffect] 渲染带霓虹发光的统计数据卡片，
 * 居中显示一个大号数值和一个小号标签。
 *
 * @param value 统计数值文字（如 "1,234"、"89%"）
 * @param label 标签文字（如 "Users"、"Growth"）
 * @param modifier 外部修饰符
 * @param glowColor 霓虹发光颜色，默认 [GlassTheme.NeonCyan]
 * @param shape 卡片形状，默认 16dp 圆角
 */
@Composable
fun GlassStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    glowColor: Color = GlassTheme.NeonCyan,
    shape: Shape = RoundedRectangle(16.dp),
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = glowColor.copy(alpha = 0.08f),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, glowColor.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = value,
                color = glowColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                color = GlassTheme.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * GlassInfoCard — 信息展示卡片
 *
 * 使用 [glassEffect] 渲染半透明玻璃质感的信息卡片，
 * 显示标题和内容文字。
 *
 * @param title 标题文字
 * @param content 内容文字
 * @param modifier 外部修饰符
 * @param shape 卡片形状，默认 16dp 圆角
 */
@Composable
fun GlassInfoCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedRectangle(16.dp),
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = GlassTheme.GlassSurface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, GlassTheme.GlassBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                color = GlassTheme.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = content,
                color = GlassTheme.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

/**
 * GlassFeatureCard — 功能特性展示卡片
 *
 * 使用 [liquidGlassEffect] 渲染液态玻璃质感的功能卡片，
 * 支持自定义图标、标题和描述。
 *
 * @param title 标题文字
 * @param description 描述文字
 * @param modifier 外部修饰符
 * @param primaryColor 主渐变色，默认 [GlassTheme.NeonPurple]
 * @param secondaryColor 副渐变色，默认 [GlassTheme.NeonCyan]
 * @param shape 卡片形状，默认 20dp 圆角
 * @param icon 图标内容（Composable slot）
 */
@Composable
fun GlassFeatureCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = GlassTheme.NeonPurple,
    secondaryColor: Color = GlassTheme.NeonCyan,
    shape: Shape = RoundedRectangle(20.dp),
    icon: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = primaryColor.copy(alpha = 0.10f),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (icon != null) {
                icon()
            }
            Text(
                text = title,
                color = GlassTheme.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                color = GlassTheme.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}
