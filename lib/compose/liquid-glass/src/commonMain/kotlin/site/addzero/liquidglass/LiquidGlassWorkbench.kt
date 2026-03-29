package site.addzero.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class LiquidGlassBackdropPalette(
    val overlayTop: Color,
    val overlayMiddle: Color,
    val overlayBottom: Color,
    val topLeftGlow: Color,
    val topLeftGlowEdge: Color,
    val topRightGlow: Color,
    val topRightGlowEdge: Color,
    val bottomGlow: Color,
    val bottomGlowEdge: Color,
)

object LiquidGlassWorkbenchDefaults {
    val contentColors = LiquidGlassContentColors(
        textPrimary = Color(0xFF162235),
        textSecondary = Color(0xFF42516D),
        textMuted = Color(0xFF71809D),
    )

    val sidebar = LiquidGlassDefaults.sidebar.copy(
        frost = 18.dp,
        refraction = 0.66f,
        curve = 0.34f,
        edge = 0.006f,
        tint = Color(0xFF8BB8FF).copy(alpha = 0.026f),
        saturation = 1.02f,
        dispersion = 0.010f,
        shape = RoundedCornerShape(30.dp),
        surfaceColor = Color.White.copy(alpha = 0.17f),
        borderColor = Color.White.copy(alpha = 0.15f),
    )

    val sidebarItem = LiquidGlassDefaults.sidebarItem.copy(
        frost = 14.dp,
        refraction = 0.58f,
        curve = 0.20f,
        edge = 0.004f,
        tint = Color(0xFF9AC5FF).copy(alpha = 0.010f),
        saturation = 1.01f,
        dispersion = 0.008f,
        shape = RoundedCornerShape(20.dp),
        surfaceColor = Color.White.copy(alpha = 0.08f),
        borderColor = Color.White.copy(alpha = 0.08f),
    )

    val sidebarItemSelected = LiquidGlassDefaults.sidebarItemSelected.copy(
        frost = 16.dp,
        refraction = 0.70f,
        curve = 0.28f,
        edge = 0.006f,
        tint = Color(0xFF71A8FF).copy(alpha = 0.052f),
        saturation = 1.05f,
        dispersion = 0.016f,
        shape = RoundedCornerShape(20.dp),
        surfaceColor = Color.White.copy(alpha = 0.13f),
        borderColor = Color.White.copy(alpha = 0.15f),
    )

    val section = LiquidGlassDefaults.card.copy(
        frost = 18.dp,
        refraction = 0.50f,
        curve = 0.16f,
        edge = 0.0012f,
        tint = Color(0xFFF7FBFF).copy(alpha = 0.008f),
        saturation = 1.01f,
        dispersion = 0.0022f,
        shape = RoundedCornerShape(22.dp),
        surfaceColor = Color.White.copy(alpha = 0.11f),
        borderColor = Color.White.copy(alpha = 0.08f),
    )

    /** 主内容区默认直接复用侧边栏材质，避免出现额外一层偏白底板。 */
    val workspace = sidebar

    val backdrop = LiquidGlassBackdropPalette(
        overlayTop = Color(0xFF06101D).copy(alpha = 0.24f),
        overlayMiddle = Color(0xFF081425).copy(alpha = 0.34f),
        overlayBottom = Color(0xFF0A1930).copy(alpha = 0.44f),
        topLeftGlow = Color(0xFF8FE7FF).copy(alpha = 0.10f),
        topLeftGlowEdge = Color(0xFF6AAFFF).copy(alpha = 0.07f),
        topRightGlow = Color(0xFF8FD7FF).copy(alpha = 0.09f),
        topRightGlowEdge = Color(0xFF6FB9FF).copy(alpha = 0.06f),
        bottomGlow = Color(0xFF58D8FF).copy(alpha = 0.08f),
        bottomGlowEdge = Color(0xFF6EA8FF).copy(alpha = 0.06f),
    )

    fun metric(accent: Color): LiquidGlassSpec {
        return LiquidGlassDefaults.sidebarItemSelected.copy(
            frost = 15.dp,
            refraction = 0.60f,
            curve = 0.20f,
            edge = 0.005f,
            tint = accent.copy(alpha = 0.048f),
            saturation = 1.03f,
            dispersion = 0.010f,
            shape = RoundedCornerShape(18.dp),
            surfaceColor = Color.White.copy(alpha = 0.12f),
            borderColor = Color.White.copy(alpha = 0.10f),
        )
    }

    fun pill(accent: Color): LiquidGlassSpec {
        return LiquidGlassDefaults.button.copy(
            frost = 14.dp,
            refraction = 0.56f,
            curve = 0.20f,
            edge = 0.005f,
            tint = accent.copy(alpha = 0.042f),
            saturation = 1.02f,
            dispersion = 0.009f,
            shape = RoundedCornerShape(999.dp),
            surfaceColor = Color.White.copy(alpha = 0.11f),
            borderColor = Color.White.copy(alpha = 0.10f),
        )
    }
}

@Composable
fun LiquidGlassWorkbenchRoot(
    modifier: Modifier = Modifier,
    backdrop: LiquidGlassBackdropPalette = LiquidGlassWorkbenchDefaults.backdrop,
    wallpaper: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    LiquidGlassRoot(
        modifier = modifier,
        background = {
            LiquidGlassWorkbenchBackdrop(
                backdrop = backdrop,
                wallpaper = wallpaper,
            )
        },
        content = {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                content()
            }
        },
    )
}

@Composable
private fun BoxScope.LiquidGlassWorkbenchBackdrop(
    backdrop: LiquidGlassBackdropPalette,
    wallpaper: (@Composable BoxScope.() -> Unit)?,
) {
    wallpaper?.invoke(this)
    Box(modifier = Modifier.fillMaxSize().workbenchBackdropBase(backdrop))
    Box(modifier = workbenchTopLeftAtmosphere(backdrop))
    Box(modifier = workbenchTopRightAtmosphere(backdrop))
    Box(modifier = workbenchBottomGlowAtmosphere(backdrop))
}

/** 工作台底色：用冷蓝雾层把背景压实，避免液态玻璃漂成一整块白板。 */
private fun Modifier.workbenchBackdropBase(
    backdrop: LiquidGlassBackdropPalette,
): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                backdrop.overlayTop,
                backdrop.overlayMiddle,
                backdrop.overlayBottom,
            ),
        ),
    )
}

/** 左上主光斑：给工作台入口一个更柔和的冷光焦点。 */
private fun BoxScope.workbenchTopLeftAtmosphere(
    backdrop: LiquidGlassBackdropPalette,
): Modifier {
    return Modifier.align(Alignment.TopStart)
        .offset(x = (-90).dp, y = (-110).dp)
        .size(340.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    backdrop.topLeftGlow,
                    backdrop.topLeftGlowEdge,
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(80.dp)
}

/** 右上云团：保持舞台有层次，但整体仍然收敛在蓝色工作台语气里。 */
private fun BoxScope.workbenchTopRightAtmosphere(
    backdrop: LiquidGlassBackdropPalette,
): Modifier {
    return Modifier.align(Alignment.TopEnd)
        .offset(x = 54.dp, y = (-40).dp)
        .width(320.dp)
        .height(250.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    backdrop.topRightGlow,
                    backdrop.topRightGlowEdge,
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(72.dp)
}

/** 底部景深光带：把主内容区托起来，避免页面发闷。 */
private fun BoxScope.workbenchBottomGlowAtmosphere(
    backdrop: LiquidGlassBackdropPalette,
): Modifier {
    return Modifier.align(Alignment.BottomCenter)
        .offset(y = 120.dp)
        .width(760.dp)
        .height(320.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    backdrop.bottomGlow,
                    backdrop.bottomGlowEdge,
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(76.dp)
}
