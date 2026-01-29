package site.addzero.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 水玻璃效果的核心实现
 * 参考 gaze-glassy 库的设计理念，提供统一的玻璃效果
 */
object GlassEffect {
    
    /**
     * 基础玻璃效果修饰符
     */
    @Composable
    fun Modifier.glassEffect(
        shape: Shape = RoundedCornerShape(16.dp),
        backgroundColor: Color = GlassColors.Surface,
        borderColor: Color = GlassColors.Border,
        borderWidth: Dp = 1.dp,
        shadowColor: Color = GlassColors.Shadow
    ): Modifier {
        return this
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.8f),
                        backgroundColor.copy(alpha = 0.4f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.8f),
                        borderColor.copy(alpha = 0.2f)
                    )
                ),
                shape = shape
            )
    }
    
    /**
     * 霓虹玻璃效果 - 带有发光边框
     */
    @Composable
    fun Modifier.neonGlassEffect(
        shape: Shape = RoundedCornerShape(16.dp),
        glowColor: Color = GlassColors.NeonCyan,
        intensity: Float = 0.6f
    ): Modifier {
        return this
            .clip(shape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.1f * intensity),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = intensity),
                        glowColor.copy(alpha = 0.3f * intensity)
                    )
                ),
                shape = shape
            )
    }
    
    /**
     * 液体玻璃效果 - 更加流动的感觉
     */
    @Composable
    fun Modifier.liquidGlassEffect(
        shape: Shape = RoundedCornerShape(24.dp),
        primaryColor: Color = GlassColors.NeonPurple,
        secondaryColor: Color = GlassColors.NeonCyan
    ): Modifier {
        return this
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.15f),
                        secondaryColor.copy(alpha = 0.08f),
                        primaryColor.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        primaryColor.copy(alpha = 0.4f),
                        secondaryColor.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = shape
            )
    }
}

/**
 * 玻璃效果专用颜色
 */
object GlassColors {
    val Surface = Color(0x1AFFFFFF)
    val Border = Color(0x40FFFFFF)
    val Shadow = Color(0x20000000)
    
    // 霓虹色彩
    val NeonCyan = Color(0xFF00F0FF)
    val NeonPurple = Color(0xFFBD00FF)
    val NeonMagenta = Color(0xFFFF0055)
    val NeonPink = Color(0xFFFF00AA)
    
    // 背景色
    val DarkBackground = Color(0xFF0F0F13)
    val DarkSurface = Color(0xFF1E1E26)
}
