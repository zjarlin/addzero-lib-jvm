package site.addzero.cupertino.workbench.sidebar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import site.addzero.appsidebar.spi.AppSidebarStyleConfig
import site.addzero.appsidebar.spi.appSidebarStyleConfig

enum class CupertinoAppSidebarVariant {
  Default,
  FlushWorkbench,
}

@Composable
fun rememberCupertinoAppSidebarStyleConfig(
  variant: CupertinoAppSidebarVariant = CupertinoAppSidebarVariant.Default,
): AppSidebarStyleConfig {
  val colors = MaterialTheme.colorScheme
  val shapes = MaterialTheme.shapes
  return remember(colors, shapes, variant) {
    when (variant) {
      CupertinoAppSidebarVariant.Default -> {
        appSidebarStyleConfig(
          contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
          emptyVerticalPadding = 8.dp,
          containerShape = RoundedCornerShape(28.dp),
          searchShape = RoundedCornerShape(16.dp),
          itemShape = RoundedCornerShape(16.dp),
          containerBackground = colors.surface.copy(alpha = 0.96f),
          containerBorder = colors.outlineVariant.copy(alpha = 0.55f),
          containerBrush = Brush.verticalGradient(
            colors = listOf(
              colors.surface.copy(alpha = 0.98f),
              colors.surfaceVariant.copy(alpha = 0.92f),
            ),
          ),
          searchBackground = colors.surfaceVariant.copy(alpha = 0.78f),
          searchBorder = colors.outlineVariant.copy(alpha = 0.45f),
          selectedBackgroundBrush = Brush.horizontalGradient(
            colors = listOf(
              colors.primary.copy(alpha = 0.20f),
              colors.primary.copy(alpha = 0.10f),
            ),
          ),
          selectedBorder = colors.primary.copy(alpha = 0.28f),
          ancestorBackground = colors.surfaceVariant.copy(alpha = 0.52f),
          ancestorBorder = colors.outlineVariant.copy(alpha = 0.22f),
          textPrimary = colors.onSurface,
          textMuted = colors.onSurfaceVariant,
          textFaint = colors.onSurfaceVariant.copy(alpha = 0.72f),
          itemStartPadding = 12.dp,
          itemIndentStep = 18.dp,
          itemVerticalPadding = 12.dp,
          itemEndPadding = 12.dp,
        )
      }

      CupertinoAppSidebarVariant.FlushWorkbench -> {
        appSidebarStyleConfig(
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
          emptyVerticalPadding = 0.dp,
          containerShape = shapes.large,
          searchShape = RoundedCornerShape(14.dp),
          itemShape = RoundedCornerShape(14.dp),
          containerBackground = colors.surface,
          containerBorder = colors.outlineVariant.copy(alpha = 0.34f),
          containerBrush = Brush.verticalGradient(
            colors = listOf(
              colors.surface.copy(alpha = 0.98f),
              colors.background.copy(alpha = 0.98f),
            ),
          ),
          searchBackground = colors.surfaceVariant.copy(alpha = 0.88f),
          searchBorder = colors.outlineVariant.copy(alpha = 0.42f),
          selectedBackgroundBrush = Brush.horizontalGradient(
            colors = listOf(
              colors.primary.copy(alpha = 0.18f),
              colors.primary.copy(alpha = 0.10f),
            ),
          ),
          selectedBorder = colors.primary.copy(alpha = 0.24f),
          ancestorBackground = colors.surfaceVariant.copy(alpha = 0.42f),
          ancestorBorder = colors.outlineVariant.copy(alpha = 0.16f),
          textPrimary = colors.onSurface,
          textMuted = colors.onSurfaceVariant,
          textFaint = colors.onSurfaceVariant.copy(alpha = 0.68f),
          itemStartPadding = 10.dp,
          itemIndentStep = 16.dp,
          itemVerticalPadding = 10.dp,
          itemEndPadding = 10.dp,
        )
      }
    }
  }
}
