package site.addzero.cupertino.workbench.header

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.cupertino.workbench.button.WorkbenchButtonSize
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchPillButton
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Text
import site.addzero.cupertino.workbench.metrics.currentWorkbenchMetrics

@Composable
fun <T> WorkbenchSceneTabs(
  items: List<T>,
  selectedId: Any?,
  onItemClick: (T) -> Unit,
  modifier: Modifier = Modifier,
  itemId: (T) -> Any,
  itemLabel: (T) -> String,
  itemIcon: (T) -> ImageVector,
) {
  val metrics = currentWorkbenchMetrics()
  Row(
    modifier = modifier
      .horizontalScroll(rememberScrollState())
      .heightIn(min = 36.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    items.forEach { item ->
      val selected = itemId(item) == selectedId
      WorkbenchPillButton(
        onClick = { onItemClick(item) },
        variant = if (selected) {
          WorkbenchButtonVariant.Default
        } else {
          WorkbenchButtonVariant.Outline
        },
        size = if (metrics.compact) {
          WorkbenchButtonSize.Sm
        } else {
          WorkbenchButtonSize.Default
        },
      ) {
        Row(
          modifier = Modifier.heightIn(min = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = itemIcon(item),
            contentDescription = null,
          )
          Text(
            text = itemLabel(item),
            style = MaterialTheme.typography.labelLarge.copy(
              lineHeight = MaterialTheme.typography.labelLarge.fontSize,
            ),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
          )
        }
      }
    }
  }
}
