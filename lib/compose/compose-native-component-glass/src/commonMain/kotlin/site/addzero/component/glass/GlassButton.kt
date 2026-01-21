package site.addzero.vibepocket.components.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 水玻璃按钮组件
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    textColor: Color = Color.White,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .then(
                if (enabled) {
                    Modifier.glassEffect(shape = shape)
                } else {
                    Modifier.glassEffect(
                        shape = shape,
                        backgroundColor = GlassColors.Surface.copy(alpha = 0.3f),
                        borderColor = GlassColors.Border.copy(alpha = 0.3f)
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            style = textStyle
        )
    }
}

/**
 * 霓虹玻璃按钮
 */
@Composable
fun NeonGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glowColor: Color = GlassColors.NeonCyan,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "neon_button_scale"
    )
    
    val glowIntensity by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 0.6f,
        animationSpec = tween(150),
        label = "neon_glow"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .neonGlassEffect(
                shape = shape,
                glowColor = glowColor,
                intensity = if (enabled) glowIntensity else 0.3f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

/**
 * 液体玻璃按钮
 */
@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primaryColor: Color = GlassColors.NeonPurple,
    secondaryColor: Color = GlassColors.NeonCyan,
    shape: Shape = RoundedCornerShape(20.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "liquid_button_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .liquidGlassEffect(
                shape = shape,
                primaryColor = if (enabled) primaryColor else primaryColor.copy(alpha = 0.5f),
                secondaryColor = if (enabled) secondaryColor else secondaryColor.copy(alpha = 0.5f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

/**
 * 扩展函数，为任何Modifier添加玻璃效果
 */
@Composable
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassColors.Surface,
    borderColor: Color = GlassColors.Border,
    borderWidth: Dp = 1.dp
): Modifier {
    return GlassEffect.run {
        this@glassEffect.glassEffect(shape, backgroundColor, borderColor, borderWidth)
    }
}

@Composable
fun Modifier.neonGlassEffect(
    shape: Shape = RoundedCornerShape(16.dp),
    glowColor: Color = GlassColors.NeonCyan,
    intensity: Float = 0.6f
): Modifier {
    return GlassEffect.run {
        this@neonGlassEffect.neonGlassEffect(shape, glowColor, intensity)
    }
}

@Composable
fun Modifier.liquidGlassEffect(
    shape: Shape = RoundedCornerShape(24.dp),
    primaryColor: Color = GlassColors.NeonPurple,
    secondaryColor: Color = GlassColors.NeonCyan
): Modifier {
    return GlassEffect.run {
        this@liquidGlassEffect.liquidGlassEffect(shape, primaryColor, secondaryColor)
    }
}