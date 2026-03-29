package site.addzero.liquidglass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class LiquidGlassButtonStyle {
    Primary,
    Secondary,
}

@Composable
fun LiquidGlassButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: LiquidGlassButtonStyle = LiquidGlassButtonStyle.Secondary,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        label = "liquidGlassButtonScale",
    )
    val spec = when (style) {
        LiquidGlassButtonStyle.Primary -> LiquidGlassDefaults.primaryButton
        LiquidGlassButtonStyle.Secondary -> LiquidGlassDefaults.button
    }
    val pressedSpec = if (pressed) {
        spec.copy(
            refraction = (spec.refraction + 0.05f).coerceAtMost(0.88f),
            dispersion = (spec.dispersion + 0.018f).coerceAtMost(0.12f),
            surfaceColor = spec.surfaceColor.copy(alpha = spec.surfaceColor.alpha * 0.72f),
            borderColor = spec.borderColor.copy(alpha = (spec.borderColor.alpha * 1.08f).coerceAtMost(1f)),
            tint = spec.tint.copy(alpha = spec.tint.alpha * 0.88f),
        )
    } else {
        spec
    }

    Row(
        modifier = modifier
            .scale(scale)
            .liquidGlassSurface(pressedSpec)
            .height(54.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LiquidGlassDefaults.textPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = text,
            color = LiquidGlassDefaults.textPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun LiquidGlassIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        label = "liquidGlassIconButtonScale",
    )
    val spec = if (pressed) {
        LiquidGlassDefaults.button.copy(
            refraction = 0.78f,
            dispersion = 0.065f,
            surfaceColor = LiquidGlassDefaults.button.surfaceColor.copy(
                alpha = LiquidGlassDefaults.button.surfaceColor.alpha * 0.70f,
            ),
        )
    } else {
        LiquidGlassDefaults.button
    }

    Row(
        modifier = modifier
            .scale(scale)
            .liquidGlassSurface(spec)
            .height(52.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LiquidGlassDefaults.textPrimary,
        )
        content()
    }
}
