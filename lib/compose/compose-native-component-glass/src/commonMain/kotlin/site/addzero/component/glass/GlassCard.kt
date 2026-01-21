package site.addzero.vibepocket.components.glass

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 基础玻璃卡片
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glassEffect(shape = shape)
    ) {
        Box(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * 霓虹玻璃卡片
 */
@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = GlassColors.NeonCyan,
    shape: Shape = RoundedCornerShape(20.dp),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.neonGlassEffect(
            shape = shape,
            glowColor = glowColor,
            intensity = 0.5f
        )
    ) {
        Box(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * 液体玻璃卡片
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    primaryColor: Color = GlassColors.NeonPurple,
    secondaryColor: Color = GlassColors.NeonCyan,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.liquidGlassEffect(
            shape = shape,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    ) {
        Box(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * 信息卡片 - 带标题和内容
 */
@Composable
fun GlassInfoCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    contentStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    titleColor: Color = Color.White,
    contentColor: Color = Color.White.copy(alpha = 0.8f)
) {
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = titleStyle,
                color = titleColor
            )
            Text(
                text = content,
                style = contentStyle,
                color = contentColor
            )
        }
    }
}

/**
 * 统计卡片 - 显示数值和标签
 */
@Composable
fun GlassStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    glowColor: Color = GlassColors.NeonCyan,
    valueStyle: TextStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    labelStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    NeonGlassCard(
        modifier = modifier,
        glowColor = glowColor,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = valueStyle,
                color = Color.White
            )
            Text(
                text = label,
                style = labelStyle,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 功能卡片 - 带图标和描述
 */
@Composable
fun GlassFeatureCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = GlassColors.NeonPurple,
    secondaryColor: Color = GlassColors.NeonCyan,
    icon: @Composable () -> Unit = {}
) {
    LiquidGlassCard(
        modifier = modifier,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}