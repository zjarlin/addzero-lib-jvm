package site.addzero.component.tree

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * `AddTree` 的尺寸、圆角和间距指标。
 */
@Immutable
data class AddTreeMetrics(
  val contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
  val rowShape: Shape = RoundedCornerShape(22.dp),
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
  val badgeShape: Shape = RoundedCornerShape(999.dp),
  val badgeHorizontalPadding: Dp = 12.dp,
  val badgeVerticalPadding: Dp = 6.dp,
)

/**
 * `AddTree` 的颜色语义。
 */
@Immutable
data class AddTreeColors(
  val rowContainer: Color,
  val rowSelectedContainer: Color,
  val rowSelectedBorder: Color,
  val rowSelectedIndicator: Color,
  val content: Color,
  val contentSelected: Color,
  val secondaryContent: Color,
  val badgeContainer: Color,
  val badgeBorder: Color,
  val badgeContent: Color,
)

/**
 * `AddTree` 的默认样式入口。
 */
object AddTreeDefaults {
  val G2Metrics: AddTreeMetrics = AddTreeMetrics()
  val CompactG2Metrics: AddTreeMetrics = AddTreeMetrics(
    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    rowShape = RoundedCornerShape(18.dp),
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
   * 生成 G2 风格的默认配色。
   */
  @Composable
  fun g2Colors(): AddTreeColors {
    val scheme = MaterialTheme.colorScheme
    return AddTreeColors(
      rowContainer = Color.Transparent,
      rowSelectedContainer = scheme.surfaceVariant.copy(alpha = 0.46f),
      rowSelectedBorder = scheme.outlineVariant.copy(alpha = 0.72f),
      rowSelectedIndicator = scheme.onSurface,
      content = scheme.onSurface,
      contentSelected = scheme.onSurface,
      secondaryContent = scheme.onSurfaceVariant,
      badgeContainer = scheme.surface.copy(alpha = 0.92f),
      badgeBorder = scheme.outlineVariant.copy(alpha = 0.74f),
      badgeContent = scheme.onSurfaceVariant,
    )
  }


}

/**
 * 树节点尾部的统一角标样式。
 */
@Composable
fun AddTreeBadge(
  text: String,
  modifier: Modifier = Modifier,
  metrics: AddTreeMetrics = AddTreeDefaults.G2Metrics,
  colors: AddTreeColors? = null,
) {
  val resolvedColors = colors ?: AddTreeDefaults.g2Colors()
  Surface(
    modifier = modifier,
    shape = metrics.badgeShape,
    color = resolvedColors.badgeContainer,
    border = BorderStroke(1.dp, resolvedColors.badgeBorder),
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
