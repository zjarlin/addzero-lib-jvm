package site.addzero.appsidebar.shadcn

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import site.addzero.appsidebar.spi.AppSidebarStyleConfig
import site.addzero.appsidebar.spi.appSidebarStyleConfig
import site.addzero.themes.colors
import site.addzero.themes.radius

enum class ShadcnAppSidebarVariant {
    Default,
    FlushWorkbench,
}

@Composable
fun rememberShadcnAppSidebarStyleConfig(
    variant: ShadcnAppSidebarVariant = ShadcnAppSidebarVariant.Default,
): AppSidebarStyleConfig {
    val colors = MaterialTheme.colors
    val radius = MaterialTheme.radius
    return remember(colors, radius, variant) {
        when (variant) {
            ShadcnAppSidebarVariant.Default -> {
                appSidebarStyleConfig(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                    emptyVerticalPadding = 8.dp,
                    containerShape = RoundedCornerShape(radius.xl),
                    searchShape = RoundedCornerShape(radius.lg),
                    itemShape = RoundedCornerShape(radius.lg),
                    containerBackground = colors.sidebar.copy(alpha = 0.94f),
                    containerBorder = colors.sidebarBorder.copy(alpha = 0.72f),
                    containerBrush = Brush.verticalGradient(
                        colors = listOf(
                            colors.sidebar.copy(alpha = 0.98f),
                            colors.background.copy(alpha = 0.92f),
                        ),
                    ),
                    searchBackground = colors.sidebarAccent.copy(alpha = 0.88f),
                    searchBorder = colors.sidebarBorder.copy(alpha = 0.64f),
                    selectedBackgroundBrush = Brush.horizontalGradient(
                        colors = listOf(
                            colors.sidebarPrimary.copy(alpha = 0.28f),
                            colors.sidebarPrimary.copy(alpha = 0.18f),
                        ),
                    ),
                    selectedBorder = colors.sidebarRing.copy(alpha = 0.32f),
                    ancestorBackground = colors.sidebarAccent.copy(alpha = 0.52f),
                    ancestorBorder = colors.sidebarBorder.copy(alpha = 0.24f),
                    textPrimary = colors.sidebarForeground.copy(alpha = 0.96f),
                    textMuted = colors.sidebarForeground.copy(alpha = 0.72f),
                    textFaint = colors.sidebarForeground.copy(alpha = 0.52f),
                    itemStartPadding = 12.dp,
                    itemIndentStep = 18.dp,
                    itemVerticalPadding = 12.dp,
                    itemEndPadding = 12.dp,
                )
            }

            ShadcnAppSidebarVariant.FlushWorkbench -> {
                appSidebarStyleConfig(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    emptyVerticalPadding = 0.dp,
                    containerShape = RoundedCornerShape(0.dp),
                    searchShape = RoundedCornerShape(radius.md),
                    itemShape = RoundedCornerShape(radius.md),
                    containerBackground = colors.sidebar,
                    containerBorder = colors.sidebarBorder.copy(alpha = 0.34f),
                    containerBrush = Brush.verticalGradient(
                        colors = listOf(
                            colors.sidebar.copy(alpha = 0.98f),
                            colors.background.copy(alpha = 0.98f),
                        ),
                    ),
                    searchBackground = colors.sidebarAccent.copy(alpha = 0.94f),
                    searchBorder = colors.sidebarBorder.copy(alpha = 0.42f),
                    selectedBackgroundBrush = Brush.horizontalGradient(
                        colors = listOf(
                            colors.sidebarPrimary.copy(alpha = 0.24f),
                            colors.sidebarPrimary.copy(alpha = 0.16f),
                        ),
                    ),
                    selectedBorder = colors.sidebarRing.copy(alpha = 0.28f),
                    ancestorBackground = colors.sidebarAccent.copy(alpha = 0.40f),
                    ancestorBorder = colors.sidebarBorder.copy(alpha = 0.14f),
                    textPrimary = colors.sidebarForeground.copy(alpha = 0.97f),
                    textMuted = colors.sidebarForeground.copy(alpha = 0.74f),
                    textFaint = colors.sidebarForeground.copy(alpha = 0.50f),
                    itemStartPadding = 10.dp,
                    itemIndentStep = 16.dp,
                    itemVerticalPadding = 10.dp,
                    itemEndPadding = 10.dp,
                )
            }
        }
    }
}
