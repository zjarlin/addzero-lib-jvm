package site.addzero.component.tree

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.addzero.compose.applecorner.AppleRoundedDefaults
import site.addzero.compose.applecorner.appleRounded
import site.addzero.compose.applecorner.appleRoundedShape

/**
 * `AddTree` 的尺寸、圆角和间距指标。
 */
@Immutable
data class AddTreeMetrics(
  val contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
  val rowShape: Shape = appleRoundedShape(AppleRoundedDefaults.Large),
  val rowMinHeight: Dp = 48.dp,
  val rowHorizontalPadding: Dp = 14.dp,
  val rowVerticalPadding: Dp = 10.dp,
  val rowSpacing: Dp = 6.dp,
  val levelIndent: Dp = 28.dp,
  val sideInset: Dp = 6.dp,
  val toggleSlotWidth: Dp = 18.dp,
  val contentSpacing: Dp = 10.dp,
  val iconSize: Dp = 20.dp,
  val expandIconSize: Dp = 18.dp,
  val selectedBorderWidth: Dp = 1.dp,
  val selectedIndicatorWidth: Dp = 3.dp,
  val selectedIndicatorHeight: Dp = 28.dp,
  val selectedIndicatorSpacing: Dp = 10.dp,
  val badgeShape: Shape = appleRoundedShape(AppleRoundedDefaults.Pill),
  val badgeHorizontalPadding: Dp = 12.dp,
  val badgeVerticalPadding: Dp = 6.dp,
)

/**
 * `AddTree` 的颜色语义。
 */
@Immutable
data class AddTreeColors(
  val rowContainer: Color,
  val rowHoveredContainer: Color,
  val rowSelectedContainer: Color,
  val rowSelectedBorder: Color,
  val rowSelectedIndicator: Color,
  val content: Color,
  val contentHovered: Color,
  val contentSelected: Color,
  val secondaryContent: Color,
  val secondaryContentHovered: Color,
  val badgeContainer: Color,
  val badgeBorder: Color,
  val badgeContent: Color,
)

/**
 * `AddTree` 的默认样式入口。
 */
object AddTreeDefaults {
  val AppleRoundedMetrics = AddTreeMetrics()
  val CompactAppleRoundedMetrics = AddTreeMetrics(
    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    rowShape = appleRoundedShape(AppleRoundedDefaults.Medium),
    rowMinHeight = 40.dp,
    rowHorizontalPadding = 12.dp,
    rowVerticalPadding = 8.dp,
    rowSpacing = 4.dp,
    levelIndent = 22.dp,
    sideInset = 4.dp,
    toggleSlotWidth = 14.dp,
    contentSpacing = 8.dp,
    iconSize = 18.dp,
    expandIconSize = 16.dp,
    selectedIndicatorHeight = 22.dp,
    selectedIndicatorSpacing = 8.dp,
    badgeHorizontalPadding = 10.dp,
    badgeVerticalPadding = 4.dp,
  )

  /**
   * 生成苹果圆角风格的默认配色。
   */
  @Composable
  fun appleRoundedColors(): AddTreeColors {
    val scheme = MaterialTheme.colorScheme
    return AddTreeColors(
      rowContainer = Color.Transparent,
      rowHoveredContainer = scheme.surfaceVariant.copy(alpha = 0.28f),
      rowSelectedContainer = scheme.surfaceVariant.copy(alpha = 0.46f),
      rowSelectedBorder = scheme.outlineVariant.copy(alpha = 0.72f),
      rowSelectedIndicator = scheme.onSurface,
      content = scheme.onSurface,
      contentHovered = scheme.onSurface,
      contentSelected = scheme.onSurface,
      secondaryContent = scheme.onSurfaceVariant,
      secondaryContentHovered = scheme.onSurface,
      badgeContainer = scheme.surface.copy(alpha = 0.92f),
      badgeBorder = scheme.outlineVariant.copy(alpha = 0.74f),
      badgeContent = scheme.onSurfaceVariant,
    )
  }

  @Deprecated(
    message = "Use AppleRoundedMetrics. Here 'G2' referred to Apple-style rounded corners.",
    replaceWith = ReplaceWith("AppleRoundedMetrics"),
  )
  val G2Metrics
      get() = AppleRoundedMetrics

  @Deprecated(
    message = "Use CompactAppleRoundedMetrics. Here 'G2' referred to Apple-style rounded corners.",
    replaceWith = ReplaceWith("CompactAppleRoundedMetrics"),
  )
  val CompactG2Metrics
      get() = CompactAppleRoundedMetrics

  @Deprecated(
    message = "Use appleRoundedColors(). Here 'G2' referred to Apple-style rounded corners.",
    replaceWith = ReplaceWith("appleRoundedColors()"),
  )
  @Composable
  fun g2Colors(): AddTreeColors = appleRoundedColors()

}

/**
 * 树节点尾部的统一角标样式。
 */
@Composable
fun AddTreeBadge(
  text: String,
  modifier: Modifier = Modifier,
  metrics: AddTreeMetrics = AddTreeDefaults.AppleRoundedMetrics,
  colors: AddTreeColors? = null,
) {
  val resolvedColors = colors ?: AddTreeDefaults.appleRoundedColors()
  Box(
    modifier = modifier
      .appleRounded(
        shape = metrics.badgeShape,
        containerColor = resolvedColors.badgeContainer,
        border = BorderStroke(1.dp, resolvedColors.badgeBorder),
      ),
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(
        horizontal = metrics.badgeHorizontalPadding,
        vertical = metrics.badgeVerticalPadding,
      ),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Medium,
      color = resolvedColors.badgeContent,
    )
  }
}
